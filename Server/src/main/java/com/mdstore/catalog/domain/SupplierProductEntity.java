package com.mdstore.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "supplier_products",
        uniqueConstraints = @UniqueConstraint(columnNames = {"supplier_code", "external_code"}),
        indexes = @Index(name = "idx_supplier_products_product_active_price", columnList = "product_id, is_active, supply_price")
)
public class SupplierProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "supplier_code", nullable = false)
    private String supplierCode;

    @Column(name = "external_code", nullable = false)
    private String externalCode;

    @Column(name = "supply_price", nullable = false)
    private BigDecimal supplyPrice;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
    
    @Column(name = "flash_sale_id")
    private String flashSaleId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SupplierProductEntity() {
    }

    public SupplierProductEntity(Long productId, String supplierCode, String externalCode, BigDecimal supplyPrice, boolean isActive, String flashSaleId) {
        this.productId = productId;
        this.supplierCode = supplierCode;
        this.externalCode = externalCode;
        this.supplyPrice = supplyPrice;
        this.isActive = isActive;
        this.flashSaleId = flashSaleId;
    }

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public String getExternalCode() {
        return externalCode;
    }

    public BigDecimal getSupplyPrice() {
        return supplyPrice;
    }

    public void setSupplyPrice(BigDecimal supplyPrice) {
        this.supplyPrice = supplyPrice;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
    
    public String getFlashSaleId() {
        return flashSaleId;
    }

    public void setFlashSaleId(String flashSaleId) {
        this.flashSaleId = flashSaleId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
