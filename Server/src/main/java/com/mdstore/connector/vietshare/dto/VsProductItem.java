package com.mdstore.connector.vietshare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Ánh xạ 1 item trong response của GET /v1/products (hoặc GET /v1/catalog).
 * Xem docs/04-suppliers/vietshare.md — API 1 — Lấy toàn bộ danh sách.
 */
public record VsProductItem(
        /** external_code — dùng khi đặt hàng (product_code trong POST /v1/orders) */
        String id,

        String name,

        /**
         * Giá bán của VietShare đã gồm markup của họ.
         * MDStore coi đây là "giá nhập" → lưu vào supplier_products.supply_price.
         * Không phải giá vốn thật. (Q-002)
         */
        BigDecimal price,

        /** Số lượng tồn kho. Không lưu DB, chỉ dùng để tính is_active. */
        int stock,

        @JsonProperty("is_active")
        boolean isActive
) {}
