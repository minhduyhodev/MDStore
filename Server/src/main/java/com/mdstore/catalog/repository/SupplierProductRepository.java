package com.mdstore.catalog.repository;

import com.mdstore.catalog.domain.SupplierProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierProductRepository extends JpaRepository<SupplierProductEntity, Long> {
    
    Optional<SupplierProductEntity> findBySupplierCodeAndExternalCode(String supplierCode, String externalCode);

    java.util.List<SupplierProductEntity> findByProductIdAndIsActiveTrueOrderBySupplyPriceAsc(Long productId);
}
