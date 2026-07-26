package com.mdstore.order.service;

public record OrderClaim(Long orderId, String orderNo, String claimToken) {
}
