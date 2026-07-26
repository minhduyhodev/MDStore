package com.mdstore.order.service;

import com.mdstore.common.web.ApiException;
import com.mdstore.common.web.ErrorCode;

public class OrderOutOfStockException extends ApiException {

    public OrderOutOfStockException() {
        super(ErrorCode.ORDER_OUT_OF_STOCK);
    }
}
