package com.mdstore.order.service;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "mdstore.orders.retry")
public record OrderRetryProperties(
        boolean enabled,
        Duration pollDelay,
        int batchSize,
        Duration leaseDuration,
        Duration retryAfterCap,
        List<Duration> delays
) {
    public OrderRetryProperties {
        delays = List.copyOf(delays);
        if (delays.isEmpty()) {
            throw new IllegalArgumentException("At least one retry delay is required");
        }
        if (pollDelay == null || pollDelay.isNegative() || pollDelay.isZero()) {
            throw new IllegalArgumentException("Retry poll delay must be positive");
        }
        if (batchSize < 1) {
            throw new IllegalArgumentException("Retry batch size must be positive");
        }
        if (leaseDuration == null || leaseDuration.isNegative() || leaseDuration.isZero()) {
            throw new IllegalArgumentException("Retry lease duration must be positive");
        }
        if (retryAfterCap == null || retryAfterCap.isNegative() || retryAfterCap.isZero()) {
            throw new IllegalArgumentException("Retry-After cap must be positive");
        }
        if (delays.stream().anyMatch(delay -> delay == null || delay.isNegative() || delay.isZero())) {
            throw new IllegalArgumentException("Every retry delay must be positive");
        }
    }

    public int maxRetries() {
        return delays.size();
    }
}
