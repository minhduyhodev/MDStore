package com.mdstore.catalog.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdstore.catalog.domain.SupplierProductEntity;
import com.mdstore.catalog.repository.SupplierProductRepository;
import com.mdstore.connector.ConnectorRegistry;
import com.mdstore.connector.SupplierConnector;
import com.mdstore.connector.SupplierProduct;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogSyncServiceTest {

    @Mock
    private ConnectorRegistry connectorRegistry;

    @Mock
    private SupplierProductRepository repository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SupplierConnector supplierConnector;

    @InjectMocks
    private CatalogSyncService catalogSyncService;

    @Test
    void syncCatalog_updatesExistingProductsAndCaches() throws Exception {
        // Arrange
        when(connectorRegistry.getAllConnectors()).thenReturn(List.of(supplierConnector));
        when(supplierConnector.getSupplierCode()).thenReturn("VIETSHARE");

        SupplierProduct dto1 = new SupplierProduct("EXT-1", "Product 1", new BigDecimal("100"), true, "FS1");
        SupplierProduct dto2 = new SupplierProduct("EXT-UNMAPPED", "Product 2", new BigDecimal("200"), true, null);
        when(supplierConnector.fetchCatalog()).thenReturn(List.of(dto1, dto2));

        SupplierProductEntity existingEntity = new SupplierProductEntity(1L, "VIETSHARE", "EXT-1", new BigDecimal("50"), true, null);
        when(repository.findBySupplierCodeAndExternalCode("VIETSHARE", "EXT-1")).thenReturn(Optional.of(existingEntity));
        when(repository.findBySupplierCodeAndExternalCode("VIETSHARE", "EXT-UNMAPPED")).thenReturn(Optional.empty());

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(any())).thenReturn("[]");

        // Act
        catalogSyncService.syncCatalog();

        // Assert
        ArgumentCaptor<SupplierProductEntity> entityCaptor = ArgumentCaptor.forClass(SupplierProductEntity.class);
        verify(repository).save(entityCaptor.capture());
        
        SupplierProductEntity saved = entityCaptor.getValue();
        assertThat(saved.getExternalCode()).isEqualTo("EXT-1");
        assertThat(saved.getSupplyPrice()).isEqualByComparingTo("100");
        assertThat(saved.getFlashSaleId()).isEqualTo("FS1");

        // Unmapped product is ignored
        verify(repository, never()).save(org.mockito.ArgumentMatchers.argThat(
                entity -> entity.getExternalCode().equals("EXT-UNMAPPED")
        ));

        // Cached
        verify(valueOperations).set(eq("catalog:VIETSHARE"), eq("[]"), any());
    }
}
