package com.mdstore.connector;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface chung cho mọi nhà cung cấp tài khoản số (ADR-004: Adapter Pattern).
 * Thêm supplier mới = viết 1 class mới implement interface này, không sửa business logic.
 */
public interface SupplierConnector {

    /**
     * Mã định danh duy nhất của supplier — dùng để tra trong ConnectorRegistry.
     * Phải khớp với suppliers.code trong DB.
     * VD: "VIETSHARE"
     */
    String getSupplierCode();

    /**
     * Lấy toàn bộ catalog sản phẩm từ supplier.
     * Được gọi bởi CatalogSyncService mỗi 10-30 giây (Flow 3).
     *
     * @return danh sách sản phẩm hiện có (is_active=true, stock>0)
     * @throws SupplierException nếu API trả lỗi không thể recover
     */
    List<SupplierProduct> fetchCatalog();

    /**
     * Đặt hàng tại supplier.
     * Gọi khi người dùng MDStore thanh toán thành công (Flow 1).
     *
     * @param request thông tin đơn hàng, BẮT BUỘC có idempotencyKey (ADR-005)
     * @return kết quả đặt hàng nếu thành công
     * @throws SupplierException với mã lỗi cụ thể cho từng tình huống (PRICE_CHANGED, OUT_OF_STOCK, etc.)
     */
    OrderResult placeOrder(OrderRequest request);
}
