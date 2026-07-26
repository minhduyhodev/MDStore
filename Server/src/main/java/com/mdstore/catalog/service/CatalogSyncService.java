package com.mdstore.catalog.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdstore.catalog.domain.SupplierProductEntity;
import com.mdstore.catalog.repository.SupplierProductRepository;
import com.mdstore.connector.ConnectorRegistry;
import com.mdstore.connector.SupplierConnector;
import com.mdstore.connector.SupplierProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Định kỳ đồng bộ danh sách sản phẩm từ các Supplier (Flow 3).
 * Kết quả được lưu vào DB (nếu sản phẩm đã được map) và ghi đè vào Redis cache.
 */
@Service
public class CatalogSyncService {

    private static final Logger log = LoggerFactory.getLogger(CatalogSyncService.class);
    private static final String REDIS_PREFIX = "catalog:";
    private static final Duration CACHE_TTL = Duration.ofSeconds(60);

    private final ConnectorRegistry connectorRegistry;
    private final SupplierProductRepository repository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CatalogSyncService(ConnectorRegistry connectorRegistry,
                              SupplierProductRepository repository,
                              StringRedisTemplate redisTemplate,
                              ObjectMapper objectMapper) {
        this.connectorRegistry = connectorRegistry;
        this.repository = repository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRateString = "${mdstore.sync.catalog.rate:30000}")
    @Transactional
    public void syncCatalog() {
        for (SupplierConnector connector : connectorRegistry.getAllConnectors()) {
            String supplierCode = connector.getSupplierCode();
            log.info("Starting catalog sync for supplier: {}", supplierCode);

            try {
                List<SupplierProduct> products = connector.fetchCatalog();
                int updatedCount = 0;

                for (SupplierProduct dto : products) {
                    Optional<SupplierProductEntity> optEntity = repository.findBySupplierCodeAndExternalCode(
                            supplierCode, dto.externalCode()
                    );

                    if (optEntity.isPresent()) {
                        SupplierProductEntity entity = optEntity.get();
                        boolean priceChanged = entity.getSupplyPrice().compareTo(dto.supplyPrice()) != 0;
                        boolean statusChanged = entity.isActive() != dto.isActive();
                        boolean flashSaleChanged = (entity.getFlashSaleId() == null && dto.flashSaleId() != null) || 
                                                   (entity.getFlashSaleId() != null && !entity.getFlashSaleId().equals(dto.flashSaleId()));
                        
                        if (priceChanged || statusChanged || flashSaleChanged) {
                            entity.setSupplyPrice(dto.supplyPrice());
                            entity.setActive(dto.isActive());
                            entity.setFlashSaleId(dto.flashSaleId());
                            repository.save(entity);
                            updatedCount++;
                        }
                    }
                }
                
                cacheToRedis(supplierCode, products);

                log.info("Finished sync for {}. Fetched: {}, DB Updated: {}",
                        supplierCode, products.size(), updatedCount);

            } catch (Exception e) {
                log.error("Failed to sync catalog for supplier: {}", supplierCode, e);
                // Lỗi từ supplier (VD: 429, 5xx) -> bỏ qua lần này, giữ nguyên cache cũ.
            }
        }
    }

    private void cacheToRedis(String supplierCode, List<SupplierProduct> products) {
        try {
            String json = objectMapper.writeValueAsString(products);
            String key = REDIS_PREFIX + supplierCode;
            redisTemplate.opsForValue().set(key, json, CACHE_TTL);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize catalog for Redis caching", e);
        }
    }
}
