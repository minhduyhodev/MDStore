package com.mdstore.order.service;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
public class OrderRetryPolicy {

    private final OrderRetryProperties properties;
    private final Clock clock;

    public OrderRetryPolicy(OrderRetryProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    public Optional<RetrySchedule> nextRetry(int currentRetryAttempt, Optional<Duration> retryAfter) {
        if (currentRetryAttempt >= properties.maxRetries()) {
            return Optional.empty();
        }

        Duration backoff = properties.delays().get(currentRetryAttempt);
        Duration requestedDelay = retryAfter
                .filter(delay -> !delay.isNegative() && !delay.isZero())
                .orElse(Duration.ZERO);
        Duration cappedRetryAfter = requestedDelay.compareTo(properties.retryAfterCap()) > 0
                ? properties.retryAfterCap()
                : requestedDelay;
        Duration effectiveDelay = backoff.compareTo(cappedRetryAfter) >= 0
                ? backoff
                : cappedRetryAfter;

        return Optional.of(new RetrySchedule(
                currentRetryAttempt + 1,
                clock.instant().plus(effectiveDelay)
        ));
    }

    public Instant now() {
        return clock.instant();
    }

    public record RetrySchedule(int retryAttempt, Instant nextRetryAt) {
    }
}
