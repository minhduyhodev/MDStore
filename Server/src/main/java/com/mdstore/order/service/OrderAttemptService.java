package com.mdstore.order.service;

import com.mdstore.common.web.ApiException;
import com.mdstore.common.web.ErrorCode;
import com.mdstore.connector.ConnectorRegistry;
import com.mdstore.connector.OrderResult;
import com.mdstore.connector.SupplierConnector;
import com.mdstore.connector.SupplierException;
import com.mdstore.order.domain.OrderStatus;
import com.mdstore.order.web.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderAttemptService {

    private static final Logger log = LoggerFactory.getLogger(OrderAttemptService.class);

    private final ConnectorRegistry connectorRegistry;
    private final OrderPersistenceService persistenceService;
    private final OrderRetryPolicy retryPolicy;

    public OrderAttemptService(ConnectorRegistry connectorRegistry,
                               OrderPersistenceService persistenceService,
                               OrderRetryPolicy retryPolicy) {
        this.connectorRegistry = connectorRegistry;
        this.persistenceService = persistenceService;
        this.retryPolicy = retryPolicy;
    }

    public OrderResponse executeInitial(OrderAttempt attempt) {
        AttemptOutcome outcome = execute(attempt);
        return switch (outcome.type) {
            case COMPLETED -> response(attempt, OrderStatus.COMPLETED, outcome.accounts);
            case RETRY_SCHEDULED -> response(attempt, OrderStatus.PROCESSING_RETRY, null);
            case PRICE_CHANGED -> throw new OrderPriceChangedException();
            case OUT_OF_STOCK -> throw new OrderOutOfStockException();
            case FAILED -> throw new ApiException(ErrorCode.SUPPLIER_ERROR);
        };
    }

    public void executeRetry(OrderAttempt attempt) {
        execute(attempt);
    }

    private AttemptOutcome execute(OrderAttempt attempt) {
        try {
            SupplierConnector connector = connectorRegistry.getConnector(attempt.supplierCode());
            OrderResult result = connector.placeOrder(attempt.toSupplierRequest());
            persistenceService.complete(attempt, result);
            log.info("Order {} completed with supplier order {}",
                    attempt.orderNo(), result.supplierOrderId());
            return AttemptOutcome.completed(result.accountData());
        } catch (SupplierException exception) {
            log.warn("Supplier {} returned {} for order {}",
                    attempt.supplierCode(), exception.getErrorCode(), attempt.orderNo());
            return handleSupplierFailure(attempt, exception);
        }
    }

    private AttemptOutcome handleSupplierFailure(OrderAttempt attempt, SupplierException exception) {
        if (exception.getErrorCode() == SupplierException.ErrorCode.PRICE_CHANGED) {
            persistenceService.fail(attempt, OrderStatus.FAILED_PRICE_CHANGED,
                    exception.getErrorCode().name(), exception.getMessage());
            return AttemptOutcome.priceChanged();
        }
        if (exception.getErrorCode() == SupplierException.ErrorCode.OUT_OF_STOCK) {
            persistenceService.fail(attempt, OrderStatus.FAILED_OUT_OF_STOCK,
                    exception.getErrorCode().name(), exception.getMessage());
            return AttemptOutcome.outOfStock();
        }
        if (exception.isRetryable()) {
            return retryPolicy.nextRetry(attempt.retryAttempt(), exception.getRetryAfter())
                    .map(schedule -> {
                        persistenceService.scheduleRetry(attempt, schedule,
                                exception.getErrorCode().name(), exception.getMessage());
                        return AttemptOutcome.retryScheduled();
                    })
                    .orElseGet(() -> {
                        persistenceService.fail(attempt, OrderStatus.FAILED,
                                exception.getErrorCode().name(), exception.getMessage());
                        log.error("Order {} exhausted all supplier retries", attempt.orderNo());
                        return AttemptOutcome.failed();
                    });
        }

        persistenceService.fail(attempt, OrderStatus.FAILED,
                exception.getErrorCode().name(), exception.getMessage());
        log.error("Order {} failed with terminal supplier error {}",
                attempt.orderNo(), exception.getErrorCode());
        return AttemptOutcome.failed();
    }

    private OrderResponse response(OrderAttempt attempt, OrderStatus status,
                                   java.util.List<String> deliveredAccounts) {
        return new OrderResponse(
                attempt.orderNo(),
                attempt.productCode(),
                attempt.quantity(),
                attempt.totalAmount(),
                status.name(),
                deliveredAccounts
        );
    }

    private static final class AttemptOutcome {
        private final OutcomeType type;
        private final java.util.List<String> accounts;

        private AttemptOutcome(OutcomeType type, java.util.List<String> accounts) {
            this.type = type;
            this.accounts = accounts;
        }

        static AttemptOutcome completed(java.util.List<String> accounts) {
            return new AttemptOutcome(OutcomeType.COMPLETED, accounts);
        }

        static AttemptOutcome retryScheduled() {
            return new AttemptOutcome(OutcomeType.RETRY_SCHEDULED, null);
        }

        static AttemptOutcome priceChanged() {
            return new AttemptOutcome(OutcomeType.PRICE_CHANGED, null);
        }

        static AttemptOutcome outOfStock() {
            return new AttemptOutcome(OutcomeType.OUT_OF_STOCK, null);
        }

        static AttemptOutcome failed() {
            return new AttemptOutcome(OutcomeType.FAILED, null);
        }
    }

    private enum OutcomeType {
        COMPLETED,
        RETRY_SCHEDULED,
        PRICE_CHANGED,
        OUT_OF_STOCK,
        FAILED
    }
}
