package com.mdstore.order.domain;

public enum OrderStatus {
    PENDING,
    PROCESSING_RETRY,
    RETRYING,
    COMPLETED,
    FAILED_PRICE_CHANGED,
    FAILED_OUT_OF_STOCK,
    FAILED
}
