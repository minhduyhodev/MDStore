package com.mdstore.connector;

import java.time.Duration;
import java.util.Optional;

/**
 * Exception ném ra khi supplier trả lỗi có nghĩa business hoặc lỗi tạm thời đã được phân loại.
 * OrderOrchestrationService bắt exception này để quyết định hành động tiếp theo.
 */
public class SupplierException extends RuntimeException {

    public enum ErrorCode {
        PRICE_CHANGED,
        OUT_OF_STOCK,
        REQUEST_IN_PROGRESS,
        UNAUTHORIZED,
        INVALID_REQUEST,
        RATE_LIMITED,
        SERVER_ERROR,
        NETWORK_TIMEOUT,
    }

    private final ErrorCode errorCode;
    private final Duration retryAfter;

    public SupplierException(ErrorCode errorCode, String message) {
        this(errorCode, message, null, null);
    }

    public SupplierException(ErrorCode errorCode, String message, Throwable cause) {
        this(errorCode, message, cause, null);
    }

    public SupplierException(ErrorCode errorCode, String message, Throwable cause, Duration retryAfter) {
        super(message, cause);
        this.errorCode = errorCode;
        this.retryAfter = retryAfter;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Optional<Duration> getRetryAfter() {
        return Optional.ofNullable(retryAfter);
    }

    public boolean isRetryable() {
        return switch (errorCode) {
            case RATE_LIMITED, SERVER_ERROR, NETWORK_TIMEOUT, REQUEST_IN_PROGRESS -> true;
            default -> false;
        };
    }
}
