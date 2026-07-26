package com.mdstore.connector;

/**
 * Exception ném ra khi supplier trả lỗi có nghĩa business (không phải lỗi network tạm thời).
 * OrderOrchestrationService bắt exception này để quyết định hành động tiếp theo.
 */
public class SupplierException extends RuntimeException {

    public enum ErrorCode {
        PRICE_CHANGED,      // HTTP 409 — giá thay đổi, cần user confirm
        OUT_OF_STOCK,       // HTTP 409 — hết hàng
        REQUEST_IN_PROGRESS,// HTTP 409 — request đang xử lý → retry với Idempotency-Key
        UNAUTHORIZED,       // HTTP 401 — sai API key/signature → alert admin
        INVALID_REQUEST,    // HTTP 400 — lỗi payload → alert dev
        RATE_LIMITED,       // HTTP 429 → retry với backoff (ADR-006)
        SERVER_ERROR,       // HTTP 5xx → retry với backoff (ADR-006)
        NETWORK_TIMEOUT,    // network timeout → retry với backoff + giữ idempotency key
    }

    private final ErrorCode errorCode;

    public SupplierException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SupplierException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /** Trả về true nếu lỗi này nên được retry (theo ADR-006) */
    public boolean isRetryable() {
        return switch (errorCode) {
            case RATE_LIMITED, SERVER_ERROR, NETWORK_TIMEOUT, REQUEST_IN_PROGRESS -> true;
            default -> false;
        };
    }
}
