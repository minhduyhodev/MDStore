package com.mdstore.order.service;

import com.mdstore.common.web.ApiException;
import com.mdstore.common.web.ErrorCode;

/**
 * Báo giá supplier đã thay đổi sau khi trạng thái terminal đã được lưu
 * trong transaction riêng của OrderPersistenceService.
 */
public class OrderPriceChangedException extends ApiException {

    public OrderPriceChangedException() {
        super(ErrorCode.ORDER_PRICE_CHANGED);
    }
}
