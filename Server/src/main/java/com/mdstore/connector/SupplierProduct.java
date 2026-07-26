package com.mdstore.connector;

import java.math.BigDecimal;

/**
 * Model output của fetchCatalog() — đại diện 1 sản phẩm từ supplier.
 * KHÔNG phải DB entity. Mapping sang supplier_products entity ở tầng Service.
 *
 * Xem docs/04-suppliers/vietshare.md — Mapping sang MDStore.
 */
public record SupplierProduct(
        /** Mã sản phẩm bên hệ thống supplier. VD: "VS_NORDVPN_1M" */
        String externalCode,

        String name,

        /**
         * Giá nhập của MDStore (= giá bán VietShare đã gồm markup của họ).
         * Lưu vào supplier_products.supply_price.
         * MDStore cộng markup riêng khi hiển thị cho khách cuối.
         * Không có endpoint nào trả giá vốn thật — không tìm thêm. (Q-002)
         */
        BigDecimal supplyPrice,

        /** item.is_active AND item.stock > 0 */
        boolean isActive,
        
        /** Flash sale ID lấy từ API (nếu có) */
        String flashSaleId
) {}
