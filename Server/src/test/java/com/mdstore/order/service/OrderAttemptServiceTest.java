package com.mdstore.order.service;

import com.mdstore.common.web.ApiException;
import com.mdstore.connector.ConnectorRegistry;
import com.mdstore.connector.OrderRequest;
import com.mdstore.connector.OrderResult;
import com.mdstore.connector.SupplierConnector;
import com.mdstore.connector.SupplierException;
import com.mdstore.order.domain.OrderStatus;
import com.mdstore.order.web.dto.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderAttemptServiceTest {

    @Mock
    private ConnectorRegistry connectorRegistry;
    @Mock
    private SupplierConnector connector;
    @Mock
    private OrderPersistenceService persistenceService;

    private OrderRetryPolicy retryPolicy;
    private OrderAttemptService service;

    @BeforeEach
    void setUp() {
        retryPolicy = new OrderRetryPolicy(
                new OrderRetryProperties(true, Duration.ofSeconds(1), 50,
                        Duration.ofSeconds(60), Duration.ofSeconds(60),
                        List.of(Duration.ofSeconds(5), Duration.ofSeconds(15), Duration.ofSeconds(30))),
                Clock.fixed(Instant.parse("2026-07-26T10:00:00Z"), ZoneOffset.UTC));
        service = new OrderAttemptService(connectorRegistry, persistenceService, retryPolicy);
        when(connectorRegistry.getConnector("VIETSHARE")).thenReturn(connector);
    }

    @Test
    void executeInitial_whenTransientFailure_schedulesRetryAndReturnsProcessing() {
        OrderAttempt attempt = attempt(0, null);
        when(connector.placeOrder(any())).thenThrow(new SupplierException(
                SupplierException.ErrorCode.RATE_LIMITED, "rate limited", null, Duration.ofSeconds(20)));

        OrderResponse response = service.executeInitial(attempt);

        assertThat(response.status()).isEqualTo("PROCESSING_RETRY");
        assertThat(response.deliveredAccounts()).isNull();
        verify(connector, times(1)).placeOrder(any());
        verify(persistenceService).scheduleRetry(eq(attempt),
                eq(new OrderRetryPolicy.RetrySchedule(1, Instant.parse("2026-07-26T10:00:20Z"))),
                eq("RATE_LIMITED"), eq("rate limited"));
    }

    @Test
    void executeRetry_reusesOriginalIdempotencyKeyAcrossAttempts() {
        when(connector.placeOrder(any()))
                .thenThrow(new SupplierException(SupplierException.ErrorCode.SERVER_ERROR, "temporary"))
                .thenThrow(new SupplierException(SupplierException.ErrorCode.NETWORK_TIMEOUT, "timeout"))
                .thenReturn(new OrderResult("SUP-1", new BigDecimal("80"), List.of("account")));

        service.executeRetry(attempt(1, "claim-1"));
        service.executeRetry(attempt(2, "claim-2"));
        service.executeRetry(attempt(3, "claim-3"));

        ArgumentCaptor<OrderRequest> requests = ArgumentCaptor.forClass(OrderRequest.class);
        verify(connector, times(3)).placeOrder(requests.capture());
        assertThat(requests.getAllValues())
                .extracting(OrderRequest::idempotencyKey)
                .containsOnly("stable-idempotency-key");
        assertThat(requests.getAllValues())
                .extracting(OrderRequest::productExternalCode)
                .containsOnly("EXT-42");
        verify(persistenceService).complete(any(OrderAttempt.class), any(OrderResult.class));
    }

    @Test
    void executeRetry_whenThirdRetryFails_marksOrderFailedWithoutSchedulingAgain() {
        OrderAttempt attempt = attempt(3, "claim-3");
        when(connector.placeOrder(any())).thenThrow(new SupplierException(
                SupplierException.ErrorCode.SERVER_ERROR, "still unavailable"));

        service.executeRetry(attempt);

        verify(persistenceService).fail(attempt, OrderStatus.FAILED,
                "SERVER_ERROR", "still unavailable");
        verify(persistenceService, never()).scheduleRetry(any(), any(), any(), any());
    }

    @Test
    void executeInitial_whenPriceChanged_persistsTerminalStatusThenThrows() {
        OrderAttempt attempt = attempt(0, null);
        when(connector.placeOrder(any())).thenThrow(new SupplierException(
                SupplierException.ErrorCode.PRICE_CHANGED, "price changed"));

        assertThatThrownBy(() -> service.executeInitial(attempt))
                .isInstanceOf(OrderPriceChangedException.class);
        verify(persistenceService).fail(attempt, OrderStatus.FAILED_PRICE_CHANGED,
                "PRICE_CHANGED", "price changed");
    }

    @Test
    void executeInitial_whenTerminalSupplierFailure_returnsSafeApiError() {
        OrderAttempt attempt = attempt(0, null);
        when(connector.placeOrder(any())).thenThrow(new SupplierException(
                SupplierException.ErrorCode.UNAUTHORIZED, "secret supplier details"));

        assertThatThrownBy(() -> service.executeInitial(attempt))
                .isInstanceOf(ApiException.class)
                .hasMessage("Lỗi từ nhà cung cấp, đơn hàng đang được xử lý hoặc đã hoàn tiền");
        verify(persistenceService).fail(attempt, OrderStatus.FAILED,
                "UNAUTHORIZED", "secret supplier details");
    }

    private OrderAttempt attempt(int retryAttempt, String claimToken) {
        return new OrderAttempt(
                1L, "MDO-1", "42", 1, new BigDecimal("80"), "VIETSHARE",
                "EXT-42", new BigDecimal("80"), "stable-idempotency-key",
                "COUPON", "FLASH", retryAttempt, claimToken);
    }
}
