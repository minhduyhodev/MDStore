package com.mdstore.order.web.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        String orderId,
        String productCode,
        Integer quantity,
        BigDecimal totalAmount,
        String status,
        List<String> deliveredAccounts
) {}
