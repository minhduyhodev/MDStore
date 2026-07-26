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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(OrderOrchestrationService.class);

    private final SupplierProductRepository supplierProductRepository;
    private final OrderRepository orderRepository;
    private final ConnectorRegistry connectorRegistry;

    public OrderOrchestrationService(SupplierProductRepository supplierProductRepository,
                                     OrderRepository orderRepository,
                                     ConnectorRegistry connectorRegistry) {
        this.supplierProductRepository = supplierProductRepository;
        this.orderRepository = orderRepository;
        this.connectorRegistry = connectorRegistry;
    }

    @Transactional(noRollbackFor = OrderPriceChangedException.class)
    public OrderResponse placeOrder(CreateOrderRequest request) {
        Long productId;
        try {
            productId = Long.parseLong(request.productCode());
        } catch (NumberFormatException e) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Invalid product code format");
        }

        // 1. Tìm các supplier có bán sản phẩm này (đã sắp xếp giá nhập từ thấp đến cao)
        List<SupplierProductEntity> suppliers = supplierProductRepository
                .findByProductIdAndIsActiveTrueOrderBySupplyPriceAsc(productId);

        if (suppliers.isEmpty()) {
            throw new ApiException(ErrorCode.ORDER_OUT_OF_STOCK, "Sản phẩm tạm hết hàng");
        }

        // 2. Routing logic: Chọn supplier đầu tiên (giá rẻ nhất)
        SupplierProductEntity chosenSupplier = suppliers.get(0);
        log.info("Routed order for product {} to supplier {} (Price: {})",
                productId, chosenSupplier.getSupplierCode(), chosenSupplier.getSupplyPrice());

        // 3. Khởi tạo đơn hàng (trạng thái PENDING)
        String orderNo = "MDO-" + System.currentTimeMillis();
        String idempotencyKey = UUID.randomUUID().toString();
        BigDecimal totalAmount = chosenSupplier.getSupplyPrice().multiply(BigDecimal.valueOf(request.quantity()));
        
        OrderEntity order = new OrderEntity(
                orderNo,
                1L, // hardcode user ID tạm thời
                productId,
                request.quantity(),
                totalAmount,
                "PENDING",
                idempotencyKey,
                chosenSupplier.getSupplierCode()
        );
        orderRepository.save(order);

        // 4. Lấy Connector tương ứng
        SupplierConnector connector = connectorRegistry.getConnector(chosenSupplier.getSupplierCode());

        // 5. Tạo request gọi API Supplier
        OrderRequest supplierRequest = new OrderRequest(
                chosenSupplier.getExternalCode(),
                request.quantity(),
                chosenSupplier.getSupplyPrice(),
                idempotencyKey,
                request.couponCode(),
                chosenSupplier.getFlashSaleId()
        );

        // 6. Gọi Supplier API
        try {
            OrderResult result = connector.placeOrder(supplierRequest);
            
            // Nếu thành công: Cập nhật trạng thái COMPLETED
            order.setStatus("COMPLETED");
            orderRepository.save(order);
            
            log.info("Order {} completed successfully with supplier order ID {}", orderNo, result.supplierOrderId());
            
            return new OrderResponse(
                    order.getOrderNo(),
                    request.productCode(),
                    order.getQuantity(),
                    order.getTotalAmount(),
                    "COMPLETED",
                    result.accountData()
            );

        } catch (SupplierException e) {
            // Lỗi từ supplier (VD: PRICE_CHANGED, OUT_OF_STOCK, RATE_LIMITED)
            log.warn("Supplier {} returned error for order {}: {}",
                    chosenSupplier.getSupplierCode(), orderNo, e.getErrorCode());

            if (e.getErrorCode() == SupplierException.ErrorCode.PRICE_CHANGED) {
                order.setStatus("FAILED_PRICE_CHANGED");
                orderRepository.save(order);
                throw new OrderPriceChangedException();
            }

            // Các mã lỗi supplier khác được xử lý ở các task tiếp theo.
            throw new ApiException(ErrorCode.INTERNAL_SERVER_ERROR, "Lỗi từ nhà cung cấp: " + e.getMessage());
        }
    }
}
