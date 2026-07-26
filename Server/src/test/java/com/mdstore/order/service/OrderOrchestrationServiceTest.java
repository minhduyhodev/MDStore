package com.mdstore.order.service;

import com.mdstore.catalog.domain.SupplierProductEntity;
import com.mdstore.catalog.repository.SupplierProductRepository;
import com.mdstore.common.web.ApiException;
import com.mdstore.order.domain.OrderEntity;
import com.mdstore.order.web.dto.CreateOrderRequest;
import com.mdstore.order.web.dto.OrderResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderOrchestrationServiceTest {

    @Mock
    private SupplierProductRepository supplierProductRepository;

    @Mock
    private OrderPersistenceService persistenceService;

    @Mock
    private OrderAttemptService attemptService;

    @Mock
    private OrderRetryPolicy retryPolicy;

    @Test
    void placeOrder_routesToCheapestAndPersistsStableRequestSnapshot() {
        OrderOrchestrationService service = new OrderOrchestrationService(
                supplierProductRepository, persistenceService, attemptService, retryPolicy);
        CreateOrderRequest request = new CreateOrderRequest("42", 2, "DISCOUNT10");
        SupplierProductEntity cheap = new SupplierProductEntity(
                42L, "CHEAP", "EXT-1", new BigDecimal("80"), true, "FLASH");
        SupplierProductEntity expensive = new SupplierProductEntity(
                42L, "EXPENSIVE", "EXT-2", new BigDecimal("100"), true, null);
        when(supplierProductRepository.findByProductIdAndIsActiveTrueOrderBySupplyPriceAsc(42L))
                .thenReturn(List.of(cheap, expensive));

        java.time.Instant now = java.time.Instant.parse("2026-07-26T10:00:00Z");
        when(retryPolicy.now()).thenReturn(now);
        ArgumentCaptor<OrderEntity> orderCaptor = ArgumentCaptor.forClass(OrderEntity.class);
        when(persistenceService.createInitialAttempt(orderCaptor.capture(), org.mockito.ArgumentMatchers.eq(now)))
                .thenAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            return new OrderAttempt(1L, order.getOrderNo(), order.getProductId().toString(),
                    order.getQuantity(), order.getTotalAmount(), order.getSupplierCode(),
                    order.getSupplierExternalCode(), order.getMaxUnitPrice(), order.getIdempotencyKey(),
                    order.getCouponCode(), order.getFlashSaleId(), order.getRetryAttempt(), "initial-claim");
        });
        OrderResponse completed = new OrderResponse(
                "MDO-1", "42", 2, new BigDecimal("160"), "COMPLETED", List.of("account"));
        when(attemptService.executeInitial(org.mockito.ArgumentMatchers.any(OrderAttempt.class)))
                .thenReturn(completed);

        OrderResponse response = service.placeOrder(request);

        assertThat(response).isEqualTo(completed);
        OrderEntity saved = orderCaptor.getValue();
        assertThat(saved.getSupplierCode()).isEqualTo("CHEAP");
        assertThat(saved.getSupplierExternalCode()).isEqualTo("EXT-1");
        assertThat(saved.getMaxUnitPrice()).isEqualByComparingTo("80");
        assertThat(saved.getCouponCode()).isEqualTo("DISCOUNT10");
        assertThat(saved.getFlashSaleId()).isEqualTo("FLASH");
        assertThat(saved.getIdempotencyKey()).isNotBlank();
        assertThat(saved.getTotalAmount()).isEqualByComparingTo("160");
        verify(attemptService).executeInitial(org.mockito.ArgumentMatchers.argThat(
                attempt -> attempt.idempotencyKey().equals(saved.getIdempotencyKey())));
    }

    @Test
    void placeOrder_whenNoSupplierFound_throwsOutOfStockWithoutPersisting() {
        OrderOrchestrationService service = new OrderOrchestrationService(
                supplierProductRepository, persistenceService, attemptService, retryPolicy);
        when(supplierProductRepository.findByProductIdAndIsActiveTrueOrderBySupplyPriceAsc(999L))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.placeOrder(new CreateOrderRequest("999", 1, null)))
                .isInstanceOf(OrderOutOfStockException.class);
        verifyNoInteractions(persistenceService, attemptService);
    }

    @Test
    void placeOrder_whenProductCodeInvalid_throwsBadRequest() {
        OrderOrchestrationService service = new OrderOrchestrationService(
                supplierProductRepository, persistenceService, attemptService, retryPolicy);

        assertThatThrownBy(() -> service.placeOrder(new CreateOrderRequest("invalid", 1, null)))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Invalid product code");
    }
}
