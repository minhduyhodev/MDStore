package com.mdstore.connector.vietshare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record VsOrderResponse(
        String status,
        VsOrderData data
) {
    public record VsOrderData(
            @JsonProperty("order_id")
            String orderId,
            
            @JsonProperty("product_code")
            String productCode,
            
            @JsonProperty("unit_price")
            BigDecimal unitPrice,
            
            @JsonProperty("total_price")
            BigDecimal totalPrice,
            
            @JsonProperty("delivered_accounts")
            List<VsDeliveredAccount> deliveredAccounts
    ) {}

    public record VsDeliveredAccount(
            String type,
            String data
    ) {}
}
