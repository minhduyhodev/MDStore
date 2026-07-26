package com.mdstore.order.service;

import com.mdstore.catalog.domain.SupplierProductEntity;
import com.mdstore.catalog.repository.SupplierProductRepository;
import com.mdstore.common.web.ApiException;
import com.mdstore.common.web.ErrorCode;
import com.mdstore.connector.ConnectorRegistry;
import com.mdstore.connector.OrderRequest;
import com.mdstore.connector.OrderResult;
import com.mdstore.connector.SupplierConnector;
import com.mdstore.connector.SupplierException;
import com.mdstore.order.domain.OrderEntity;
import com.mdstore.order.repository.OrderRepository;
import com.mdstore.order.web.dto.CreateOrderRequest;
import com.mdstore.order.web.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderOrchestrationServiceTest {

    @Mock
    private SupplierProductRepository supplierProductRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ConnectorRegistry connectorRegistry;

    @Mock
    private SupplierConnector supplierConnector;

    @InjectMocks
    private OrderOrchestrationService service;

    @Test
    void placeOrder_withMultipleSuppliers_routesToCheapestSupplier() {
        // Arrange
        Long productId = 42L;
        CreateOrderRequest request = new CreateOrderRequest("42", 2, "DISCOUNT10");

        SupplierProductEntity cheapSupplier = new SupplierProductEntity(productId, "SUPPLIER_CHEAP", "EXT-1", new BigDecimal("80"), true, "FS_2026");
        SupplierProductEntity expensiveSupplier = new SupplierProductEntity(productId, "SUPPLIER_EXPENSIVE", "EXT-2", new BigDecimal("100"), true, null);

        when(supplierProductRepository.findByProductIdAndIsActiveTrueOrderBySupplyPriceAsc(productId))
                .thenReturn(List.of(cheapSupplier, expensiveSupplier)); // Sắp xếp theo giá tăng dần

        when(connectorRegistry.getConnector("SUPPLIER_CHEAP")).thenReturn(supplierConnector);
        when(supplierConnector.placeOrder(any(OrderRequest.class))).thenReturn(new OrderResult("SUP-ORDER-1", new BigDecimal("80"), List.of("data")));

        // Act
        OrderResponse response = service.placeOrder(request);

        // Assert
        assertThat(response.status()).isEqualTo("COMPLETED");

        // Verify routing called cheap supplier connector
        ArgumentCaptor<OrderRequest> requestCaptor = ArgumentCaptor.forClass(OrderRequest.class);
        verify(supplierConnector).placeOrder(requestCaptor.capture());
        
        OrderRequest sentRequest = requestCaptor.getValue();
        assertThat(sentRequest.productExternalCode()).isEqualTo("EXT-1");
        assertThat(sentRequest.maxUnitPrice()).isEqualByComparingTo("80");
        assertThat(sentRequest.quantity()).isEqualTo(2);
        assertThat(sentRequest.couponCode()).isEqualTo("DISCOUNT10");
        assertThat(sentRequest.flashSaleId()).isEqualTo("FS_2026");

        // Verify DB saved correctly
        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        // It's saved twice (PENDING -> COMPLETED)
        verify(orderRepository, org.mockito.Mockito.times(2)).save(orderCaptor.capture());
        
        OrderEntity finalSavedOrder = orderCaptor.getAllValues().get(1);
        assertThat(finalSavedOrder.getStatus()).isEqualTo("COMPLETED");
        assertThat(finalSavedOrder.getSupplierCode()).isEqualTo("SUPPLIER_CHEAP");
        assertThat(finalSavedOrder.getTotalAmount()).isEqualByComparingTo("160"); // 80 * 2
    }

    @Test
    void placeOrder_whenNoSupplierFound_throwsOutOfStockException() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("999", 1, null);
        when(supplierProductRepository.findByProductIdAndIsActiveTrueOrderBySupplyPriceAsc(999L))
                .thenReturn(List.of());

        // Act & Assert
        assertThatThrownBy(() -> service.placeOrder(request))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Sản phẩm tạm hết hàng");
    }

    @Test
    void placeOrder_doesNotRollbackPriceChangedStatus() throws NoSuchMethodException {
        Method method = OrderOrchestrationService.class.getMethod("placeOrder", CreateOrderRequest.class);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.noRollbackFor()).contains(OrderPriceChangedException.class);
    }

    @Test
    void placeOrder_whenSupplierPriceChanged_savesFailedStatusAndNotifiesUser() {
        // Arrange
        Long productId = 42L;
        CreateOrderRequest request = new CreateOrderRequest("42", 1, null);
        SupplierProductEntity supplier = new SupplierProductEntity(
                productId, "VIETSHARE", "EXT-1", new BigDecimal("80"), true, null);

        when(supplierProductRepository.findByProductIdAndIsActiveTrueOrderBySupplyPriceAsc(productId))
                .thenReturn(List.of(supplier));
        when(connectorRegistry.getConnector("VIETSHARE")).thenReturn(supplierConnector);
        when(supplierConnector.placeOrder(any(OrderRequest.class)))
                .thenThrow(new SupplierException(
                        SupplierException.ErrorCode.PRICE_CHANGED,
                        "Supplier price changed"));

        // Act & Assert
        assertThatThrownBy(() -> service.placeOrder(request))
                .isInstanceOf(OrderPriceChangedException.class)
                .satisfies(e -> assertThat(((ApiException) e).getErrorCode())
                        .isEqualTo(ErrorCode.ORDER_PRICE_CHANGED));

        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        verify(orderRepository, org.mockito.Mockito.times(2)).save(orderCaptor.capture());
        assertThat(orderCaptor.getAllValues().get(1).getStatus()).isEqualTo("FAILED_PRICE_CHANGED");
    }
}
