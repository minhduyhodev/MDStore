package com.mdstore.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", nullable = false, unique = true)
    private String orderNo;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "supplier_code", nullable = false)
    private String supplierCode;

    @Column(name = "supplier_external_code", nullable = false)
    private String supplierExternalCode;

    @Column(name = "max_unit_price", nullable = false)
    private BigDecimal maxUnitPrice;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "flash_sale_id")
    private String flashSaleId;

    @Column(name = "retry_attempt", nullable = false)
    private int retryAttempt;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "last_error_code")
    private String lastErrorCode;

    @Column(name = "last_error_message", length = 500)
    private String lastErrorMessage;

    @Column(name = "retry_claim_token", length = 36)
    private String retryClaimToken;

    @Column(name = "retry_lease_until")
    private Instant retryLeaseUntil;

    @Column(name = "supplier_order_id")
    private String supplierOrderId;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public OrderEntity() {
    }

    public OrderEntity(String orderNo, Long userId, Long productId, Integer quantity,
                       BigDecimal totalAmount, String idempotencyKey, String supplierCode,
                       String supplierExternalCode, BigDecimal maxUnitPrice,
                       String couponCode, String flashSaleId) {
        this.orderNo = orderNo;
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.status = OrderStatus.PENDING;
        this.idempotencyKey = idempotencyKey;
        this.supplierCode = supplierCode;
        this.supplierExternalCode = supplierExternalCode;
        this.maxUnitPrice = maxUnitPrice;
        this.couponCode = couponCode;
        this.flashSaleId = flashSaleId;
    }

    public void touch(Instant now) {
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    public void markProcessingRetry(int retryAttempt, Instant nextRetryAt,
                                    String errorCode, String errorMessage) {
        this.status = OrderStatus.PROCESSING_RETRY;
        this.retryAttempt = retryAttempt;
        this.nextRetryAt = nextRetryAt;
        this.lastErrorCode = errorCode;
        this.lastErrorMessage = sanitize(errorMessage);
        clearClaim();
    }

    public void claimRetry(String claimToken, Instant leaseUntil) {
        this.status = OrderStatus.RETRYING;
        this.retryClaimToken = claimToken;
        this.retryLeaseUntil = leaseUntil;
    }

    public void markCompleted(String supplierOrderId) {
        this.status = OrderStatus.COMPLETED;
        this.supplierOrderId = supplierOrderId;
        this.nextRetryAt = null;
        this.lastErrorCode = null;
        this.lastErrorMessage = null;
        clearClaim();
    }

    public void markFailed(OrderStatus failedStatus, String errorCode, String errorMessage) {
        if (failedStatus != OrderStatus.FAILED
                && failedStatus != OrderStatus.FAILED_PRICE_CHANGED
                && failedStatus != OrderStatus.FAILED_OUT_OF_STOCK) {
            throw new IllegalArgumentException("Invalid terminal order status: " + failedStatus);
        }
        this.status = failedStatus;
        this.nextRetryAt = null;
        this.lastErrorCode = errorCode;
        this.lastErrorMessage = sanitize(errorMessage);
        clearClaim();
    }

    public void recoverExpiredLease(Instant retryAt) {
        this.status = OrderStatus.PROCESSING_RETRY;
        this.nextRetryAt = retryAt;
        clearClaim();
    }

    private void clearClaim() {
        this.retryClaimToken = null;
        this.retryLeaseUntil = null;
    }

    private String sanitize(String message) {
        if (message == null) {
            return null;
        }
        return message.substring(0, Math.min(message.length(), 500));
    }

    public Long getId() {
        return id;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public String getSupplierExternalCode() {
        return supplierExternalCode;
    }

    public BigDecimal getMaxUnitPrice() {
        return maxUnitPrice;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public String getFlashSaleId() {
        return flashSaleId;
    }

    public int getRetryAttempt() {
        return retryAttempt;
    }

    public Instant getNextRetryAt() {
        return nextRetryAt;
    }

    public String getLastErrorCode() {
        return lastErrorCode;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public String getRetryClaimToken() {
        return retryClaimToken;
    }

    public Instant getRetryLeaseUntil() {
        return retryLeaseUntil;
    }

    public String getSupplierOrderId() {
        return supplierOrderId;
    }

    public long getVersion() {
        return version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
