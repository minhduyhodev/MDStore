package com.mdstore.order.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnProperty(prefix = "mdstore.orders.retry", name = "enabled", havingValue = "true")
public class OrderRetryWorker {

    private static final Logger log = LoggerFactory.getLogger(OrderRetryWorker.class);

    private final OrderPersistenceService persistenceService;
    private final OrderAttemptService attemptService;
    private final OrderRetryPolicy retryPolicy;

    public OrderRetryWorker(OrderPersistenceService persistenceService,
                            OrderAttemptService attemptService,
                            OrderRetryPolicy retryPolicy) {
        this.persistenceService = persistenceService;
        this.attemptService = attemptService;
        this.retryPolicy = retryPolicy;
    }

    @Scheduled(fixedDelayString = "${mdstore.orders.retry.poll-delay:1000}")
    public void processDueRetries() {
        int recovered = persistenceService.recoverExpiredLeases(retryPolicy.now());
        if (recovered > 0) {
            log.warn("Recovered {} expired order retry leases", recovered);
        }

        List<OrderClaim> claims = persistenceService.claimDueRetries(retryPolicy.now());
        for (OrderClaim claim : claims) {
            try {
                OrderAttempt attempt = persistenceService.loadClaimedAttempt(claim);
                attemptService.executeRetry(attempt);
            } catch (RuntimeException exception) {
                log.error("Unexpected retry worker failure for order {}", claim.orderNo(), exception);
            }
        }
    }
}
