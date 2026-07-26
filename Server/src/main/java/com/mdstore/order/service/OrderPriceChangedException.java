package com.mdstore.order.service;

import com.mdstore.common.web.ApiException;
import com.mdstore.common.web.ErrorCode;

/**
 * Báo giá supplier đã thay đổi sau khi trạng thái đơn hàng được lưu.
 * Transaction của OrderOrchestrationService không rollback với exception này.
 */
public class OrderPriceChangedException extends ApiException {

    public OrderPriceChangedException() {
        super(ErrorCode.ORDER_PRICE_CHANGED);
    }
}
