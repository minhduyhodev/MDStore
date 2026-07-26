package com.mdstore.order.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRetryPolicyTest {

    private static final Instant NOW = Instant.parse("2026-07-26T10:00:00Z");

    private final OrderRetryPolicy policy = new OrderRetryPolicy(
            new OrderRetryProperties(true, Duration.ofSeconds(1), 50,
                    Duration.ofSeconds(60), Duration.ofSeconds(60),
                    List.of(Duration.ofSeconds(5), Duration.ofSeconds(15), Duration.ofSeconds(30))),
            Clock.fixed(NOW, ZoneOffset.UTC)
    );

    @Test
    void nextRetry_usesConfiguredBackoffForThreeRetries() {
        assertSchedule(0, 1, Duration.ofSeconds(5));
        assertSchedule(1, 2, Duration.ofSeconds(15));
        assertSchedule(2, 3, Duration.ofSeconds(30));
        assertThat(policy.nextRetry(3, Optional.empty())).isEmpty();
    }

    @Test
    void nextRetry_usesLargerRetryAfterAndCapsIt() {
        assertThat(policy.nextRetry(0, Optional.of(Duration.ofSeconds(20))))
                .contains(new OrderRetryPolicy.RetrySchedule(1, NOW.plusSeconds(20)));
        assertThat(policy.nextRetry(1, Optional.of(Duration.ofMinutes(10))))
                .contains(new OrderRetryPolicy.RetrySchedule(2, NOW.plusSeconds(60)));
    }

    private void assertSchedule(int currentAttempt, int expectedAttempt, Duration delay) {
        assertThat(policy.nextRetry(currentAttempt, Optional.empty()))
                .contains(new OrderRetryPolicy.RetrySchedule(expectedAttempt, NOW.plus(delay)));
    }
}
