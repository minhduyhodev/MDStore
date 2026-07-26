package com.mdstore.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "delivered_accounts")
public class DeliveredAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "account_data", nullable = false, columnDefinition = "TEXT")
    private String accountData;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public DeliveredAccountEntity() {
    }

    public DeliveredAccountEntity(Long orderId, String accountData) {
        this.orderId = orderId;
        this.accountData = accountData;
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getAccountData() {
        return accountData;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
