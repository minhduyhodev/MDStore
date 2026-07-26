package com.mdstore.connector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Quản lý danh sách các SupplierConnector hiện có trong hệ thống.
 * Spring Boot tự động quét các class implement SupplierConnector và nhúng vào List.
 */
@Component
public class ConnectorRegistry {

    private static final Logger log = LoggerFactory.getLogger(ConnectorRegistry.class);
    
    private final Map<String, SupplierConnector> connectors;

    public ConnectorRegistry(List<SupplierConnector> connectorList) {
        this.connectors = connectorList.stream()
                .collect(Collectors.toMap(
                        SupplierConnector::getSupplierCode,
                        connector -> connector
                ));
        log.info("Registered {} supplier connectors: {}", 
                connectors.size(), connectors.keySet());
    }

    /**
     * Lấy connector theo mã nhà cung cấp (ví dụ: "VIETSHARE").
     * @param supplierCode Mã nhà cung cấp.
     * @return SupplierConnector tương ứng.
     * @throws IllegalArgumentException nếu mã không được hỗ trợ.
     */
    public SupplierConnector getConnector(String supplierCode) {
        SupplierConnector connector = connectors.get(supplierCode);
        if (connector == null) {
            log.error("Supplier connector not found for code: {}", supplierCode);
            throw new IllegalArgumentException("Unsupported supplier code: " + supplierCode);
        }
        return connector;
    }

    /**
     * Lấy danh sách tất cả các Connector hiện có.
     */
    public java.util.Collection<SupplierConnector> getAllConnectors() {
        return connectors.values();
    }
}
