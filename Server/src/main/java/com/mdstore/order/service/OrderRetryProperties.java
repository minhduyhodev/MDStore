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
    }

    public int maxRetries() {
        return delays.size();
    }
}
