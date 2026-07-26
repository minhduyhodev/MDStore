package com.mdstore.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "supplier_products")
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
}
