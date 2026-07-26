package com.mdstore.common.web;

/**
 * Mã lỗi thống nhất trả về cho Frontend, độc lập với mã lỗi của nhà cung cấp.
 */
public enum ErrorCode {
    // Lỗi hệ thống chung
    INTERNAL_SERVER_ERROR("Lỗi hệ thống, vui lòng thử lại sau"),
    BAD_REQUEST("Dữ liệu đầu vào không hợp lệ"),
    UNAUTHORIZED("Vui lòng đăng nhập"),
    FORBIDDEN("Không có quyền truy cập"),
    NOT_FOUND("Tài nguyên không tồn tại"),
    
    // Lỗi nghiệp vụ Order
    ORDER_PRICE_CHANGED("Giá sản phẩm đã thay đổi, vui lòng xác nhận lại"),
    ORDER_OUT_OF_STOCK("Sản phẩm đã hết hàng"),
    INSUFFICIENT_BALANCE("Số dư không đủ để thanh toán"),
    
    // Các lỗi từ Supplier (nếu cần pass-through một số lỗi chung chung)
    SUPPLIER_ERROR("Lỗi từ nhà cung cấp, đơn hàng đang được xử lý hoặc đã hoàn tiền");

    private final String defaultMessage;

    ErrorCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
