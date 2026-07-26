package com.mdstore.order.service;

import com.mdstore.connector.OrderRequest;

import java.math.BigDecimal;

public record OrderAttempt(
        Long orderId,
        String orderNo,
        String productCode,
        int quantity,
        BigDecimal totalAmount,
        String supplierCode,
        String supplierExternalCode,
        BigDecimal maxUnitPrice,
        String idempotencyKey,
        String couponCode,
        String flashSaleId,
        int retryAttempt,
        String claimToken
) {
    public OrderRequest toSupplierRequest() {
        return new OrderRequest(
                supplierExternalCode,
                quantity,
                maxUnitPrice,
                idempotencyKey,
                couponCode,
                flashSaleId
        );
    }
}
