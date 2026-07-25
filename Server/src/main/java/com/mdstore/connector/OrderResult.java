package com.mdstore.connector;

import java.math.BigDecimal;
import java.util.List;

/**
 * Kết quả đặt hàng thành công từ supplier.
 * accountData chứa thông tin tài khoản để lưu vào delivered_accounts.account_data.
 */
public record OrderResult(
        String supplierOrderId,
        BigDecimal unitPrice,
        /** Danh sách tài khoản giao, mỗi item là chuỗi dạng "email|pass" hoặc token */
        List<String> accountData
) {}
