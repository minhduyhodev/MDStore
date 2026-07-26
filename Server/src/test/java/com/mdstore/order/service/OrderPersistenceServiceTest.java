package com.mdstore.order.service;

import com.mdstore.order.domain.OrderEntity;
import com.mdstore.order.domain.OrderStatus;
import com.mdstore.order.repository.DeliveredAccountRepository;
import com.mdstore.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPersistenceServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private DeliveredAccountRepository deliveredAccountRepository;

    @Test
    void createInitialAttempt_claimsOrderBeforeSupplierCall() {
        Instant now = Instant.parse("2026-07-26T10:00:00Z");
        OrderPersistenceService service = service(now);
        OrderEntity order = order();
        when(orderRepository.save(order)).thenReturn(order);

        OrderAttempt attempt = service.createInitialAttempt(order, now);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.RETRYING);
        assertThat(order.getRetryClaimToken()).isEqualTo(attempt.claimToken());
        assertThat(order.getRetryLeaseUntil()).isEqualTo(now.plusSeconds(60));
        assertThat(attempt.idempotencyKey()).isEqualTo("stable-key");
    }

    @Test
    void recoverExpiredLease_preservesRetryCountAndIdempotencyKey() {
        Instant now = Instant.parse("2026-07-26T10:00:00Z");
        OrderPersistenceService service = service(now);
        OrderEntity order = order();
        order.claimRetry("old-claim", now.minusSeconds(1));
        when(orderRepository.findExpiredLeases(now, 50)).thenReturn(List.of(order));

        int recovered = service.recoverExpiredLeases(now);

        assertThat(recovered).isEqualTo(1);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING_RETRY);
        assertThat(order.getNextRetryAt()).isEqualTo(now);
        assertThat(order.getRetryClaimToken()).isNull();
        assertThat(order.getIdempotencyKey()).isEqualTo("stable-key");
    }

    private OrderPersistenceService service(Instant now) {
        OrderRetryProperties properties = new OrderRetryProperties(
                true, Duration.ofSeconds(1), 50, Duration.ofSeconds(60),
                Duration.ofSeconds(60), List.of(Duration.ofSeconds(5)));
        return new OrderPersistenceService(orderRepository, deliveredAccountRepository,
                properties, Clock.fixed(now, ZoneOffset.UTC));
    }

    private OrderEntity order() {
        return new OrderEntity(
                "MDO-1", 1L, 42L, 1, new BigDecimal("80"), "stable-key",
                "VIETSHARE", "EXT-42", new BigDecimal("80"), null, null);
    }
}
