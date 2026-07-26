package com.mdstore.order.service;

import com.mdstore.catalog.domain.SupplierProductEntity;
import com.mdstore.catalog.repository.SupplierProductRepository;
import com.mdstore.common.web.ApiException;
import com.mdstore.common.web.ErrorCode;
import com.mdstore.order.domain.OrderEntity;
import com.mdstore.order.web.dto.CreateOrderRequest;
import com.mdstore.order.web.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class OrderOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(OrderOrchestrationService.class);

    private final SupplierProductRepository supplierProductRepository;
    private final OrderPersistenceService persistenceService;
    private final OrderAttemptService attemptService;
    private final OrderRetryPolicy retryPolicy;

    public OrderOrchestrationService(SupplierProductRepository supplierProductRepository,
                                     OrderPersistenceService persistenceService,
                                     OrderAttemptService attemptService,
                                     OrderRetryPolicy retryPolicy) {
        this.supplierProductRepository = supplierProductRepository;
        this.persistenceService = persistenceService;
        this.attemptService = attemptService;
        this.retryPolicy = retryPolicy;
    }

    public OrderResponse placeOrder(CreateOrderRequest request) {
        Long productId = parseProductId(request.productCode());
        List<SupplierProductEntity> suppliers = supplierProductRepository
                .findByProductIdAndIsActiveTrueOrderBySupplyPriceAsc(productId);
        if (suppliers.isEmpty()) {
            throw new OrderOutOfStockException();
        }

        SupplierProductEntity chosenSupplier = suppliers.get(0);
        log.info("Routed order for product {} to supplier {} (Price: {})",
                productId, chosenSupplier.getSupplierCode(), chosenSupplier.getSupplyPrice());

        String orderNo = "MDO-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
        String idempotencyKey = UUID.randomUUID().toString();
        BigDecimal totalAmount = chosenSupplier.getSupplyPrice()
                .multiply(BigDecimal.valueOf(request.quantity()));

        OrderEntity order = new OrderEntity(
                orderNo,
                1L,
                productId,
                request.quantity(),
                totalAmount,
                idempotencyKey,
                chosenSupplier.getSupplierCode(),
                chosenSupplier.getExternalCode(),
                chosenSupplier.getSupplyPrice(),
                request.couponCode(),
                chosenSupplier.getFlashSaleId()
        );

        OrderAttempt attempt = persistenceService.createInitialAttempt(order, retryPolicy.now());
        return attemptService.executeInitial(attempt);
    }

    private Long parseProductId(String productCode) {
        try {
            return Long.parseLong(productCode);
        } catch (NumberFormatException exception) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Invalid product code format");
        }
    }
}
