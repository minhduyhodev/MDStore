package com.mdstore.order.service;

import com.mdstore.connector.OrderResult;
import com.mdstore.order.domain.DeliveredAccountEntity;
import com.mdstore.order.domain.OrderEntity;
import com.mdstore.order.domain.OrderStatus;
import com.mdstore.order.repository.DeliveredAccountRepository;
import com.mdstore.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OrderPersistenceService {

    private final OrderRepository orderRepository;
    private final DeliveredAccountRepository deliveredAccountRepository;
    private final OrderRetryProperties retryProperties;
    private final Clock clock;

    public OrderPersistenceService(OrderRepository orderRepository,
                                   DeliveredAccountRepository deliveredAccountRepository,
                                   OrderRetryProperties retryProperties,
                                   Clock clock) {
        this.orderRepository = orderRepository;
        this.deliveredAccountRepository = deliveredAccountRepository;
        this.retryProperties = retryProperties;
        this.clock = clock;
    }

    @Transactional
    public OrderAttempt createInitialAttempt(OrderEntity order, Instant now) {
        order.touch(now);
        OrderEntity saved = orderRepository.save(order);
        return claim(saved, now);
    }

    @Transactional
    public void complete(OrderAttempt attempt, OrderResult result) {
        OrderEntity order = loadCurrentAttempt(attempt);
        Instant completedAt = clock.instant();
        order.markCompleted(result.supplierOrderId());
        order.touch(completedAt);
        deliveredAccountRepository.saveAll(result.accountData().stream()
                .map(account -> new DeliveredAccountEntity(order.getId(), account, completedAt))
                .toList());
    }

    @Transactional
    public void scheduleRetry(OrderAttempt attempt, OrderRetryPolicy.RetrySchedule schedule,
                              String errorCode, String errorMessage) {
        OrderEntity order = loadCurrentAttempt(attempt);
        order.markProcessingRetry(schedule.retryAttempt(), schedule.nextRetryAt(), errorCode, errorMessage);
        order.touch(clock.instant());
    }

    @Transactional
    public void fail(OrderAttempt attempt, OrderStatus failedStatus,
                     String errorCode, String errorMessage) {
        OrderEntity order = loadCurrentAttempt(attempt);
        order.markFailed(failedStatus, errorCode, errorMessage);
        order.touch(clock.instant());
    }

    @Transactional
    public List<OrderClaim> claimDueRetries(Instant now) {
        return orderRepository.findDueForRetry(now, retryProperties.batchSize())
                .stream()
                .map(order -> toClaim(claim(order, now)))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderAttempt loadClaimedAttempt(OrderClaim claim) {
        OrderEntity order = orderRepository.findById(claim.orderId())
                .orElseThrow(() -> new IllegalStateException("Order not found: " + claim.orderNo()));
        if (order.getStatus() != OrderStatus.RETRYING
                || !claim.claimToken().equals(order.getRetryClaimToken())) {
            throw new StaleOrderAttemptException(claim.orderNo());
        }
        return toAttempt(order, claim.claimToken());
    }

    @Transactional
    public int recoverExpiredLeases(Instant now) {
        List<OrderEntity> expired = orderRepository.findExpiredLeases(
                now, retryProperties.batchSize());
        expired.forEach(order -> {
            order.recoverExpiredLease(now);
            order.touch(now);
        });
        return expired.size();
    }

    private OrderAttempt claim(OrderEntity order, Instant now) {
        String claimToken = UUID.randomUUID().toString();
        order.claimRetry(claimToken, now.plus(retryProperties.leaseDuration()));
        order.touch(now);
        return toAttempt(order, claimToken);
    }

    private OrderClaim toClaim(OrderAttempt attempt) {
        return new OrderClaim(attempt.orderId(), attempt.orderNo(), attempt.claimToken());
    }

    private OrderEntity loadCurrentAttempt(OrderAttempt attempt) {
        OrderEntity order = orderRepository.findById(attempt.orderId())
                .orElseThrow(() -> new IllegalStateException("Order not found: " + attempt.orderNo()));

        if (attempt.claimToken() == null
                || !attempt.claimToken().equals(order.getRetryClaimToken())
                || order.getStatus() != OrderStatus.RETRYING) {
            throw new StaleOrderAttemptException(attempt.orderNo());
        }
        return order;
    }

    private OrderAttempt toAttempt(OrderEntity order, String claimToken) {
        return new OrderAttempt(
                order.getId(),
                order.getOrderNo(),
                order.getProductId().toString(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getSupplierCode(),
                order.getSupplierExternalCode(),
                order.getMaxUnitPrice(),
                order.getIdempotencyKey(),
                order.getCouponCode(),
                order.getFlashSaleId(),
                order.getRetryAttempt(),
                claimToken
        );
    }

    public static class StaleOrderAttemptException extends RuntimeException {
        public StaleOrderAttemptException(String orderNo) {
            super("Order attempt is no longer current: " + orderNo);
        }
    }
}
