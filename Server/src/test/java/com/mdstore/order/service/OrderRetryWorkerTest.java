package com.mdstore.order.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderRetryWorkerTest {

    @Mock
    private OrderPersistenceService persistenceService;
    @Mock
    private OrderAttemptService attemptService;

    @Test
    void processDueRetries_recoversClaimsThenProcessesDueOrders() {
        Instant now = Instant.parse("2026-07-26T10:00:00Z");
        OrderRetryPolicy policy = new OrderRetryPolicy(
                new OrderRetryProperties(true, Duration.ofSeconds(1), 50,
                        Duration.ofSeconds(60), Duration.ofSeconds(60),
                        List.of(Duration.ofSeconds(5))),
                Clock.fixed(now, ZoneOffset.UTC));
        OrderRetryWorker worker = new OrderRetryWorker(persistenceService, attemptService, policy);
        OrderAttempt attempt = new OrderAttempt(
                1L, "MDO-1", "42", 1, new BigDecimal("80"), "VIETSHARE",
                "EXT", new BigDecimal("80"), "stable-key", null, null, 1, "claim");
        when(persistenceService.claimDueRetries(now)).thenReturn(List.of(attempt));

        worker.processDueRetries();

        InOrder order = inOrder(persistenceService, attemptService);
        order.verify(persistenceService).recoverExpiredLeases(now);
        order.verify(persistenceService).claimDueRetries(now);
        order.verify(attemptService).executeRetry(attempt);
    }
}
