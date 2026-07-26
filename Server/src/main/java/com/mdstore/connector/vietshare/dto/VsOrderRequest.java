package com.mdstore.connector.vietshare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public record VsOrderRequest(
        @JsonProperty("product_code")
        String productCode,
        
        int quantity,
        
        @JsonProperty("max_unit_price")
        BigDecimal maxUnitPrice,
        
        @JsonProperty("coupon_code")
        String couponCode,
        
        @JsonProperty("flash_sale_id")
        String flashSaleId
) {}
