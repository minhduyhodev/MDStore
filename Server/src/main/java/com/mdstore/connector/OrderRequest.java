package com.mdstore.connector;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request đặt hàng gửi tới supplier.
 * idempotencyKey BẮT BUỘC — dùng để chống trùng đơn khi retry (ADR-005).
 */
public record OrderRequest(
        String productExternalCode,
        int quantity,
        BigDecimal maxUnitPrice,
        /** UUID của orders nội bộ MDStore — KHÔNG được tạo mới khi retry (ADR-005) */
        String idempotencyKey
) {}
