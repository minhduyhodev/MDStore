package com.mdstore.order.service;

import com.mdstore.common.web.ApiException;
import com.mdstore.common.web.ErrorCode;

/**
 * Business exception used to preserve the failed order and return HTTP 409.
 */
public class OrderPriceChangedException extends ApiException {

    public OrderPriceChangedException() {
        super(ErrorCode.ORDER_PRICE_CHANGED);
    }
}
