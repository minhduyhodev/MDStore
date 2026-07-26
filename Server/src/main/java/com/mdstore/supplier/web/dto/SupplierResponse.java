package com.mdstore.supplier.web.dto;

public record SupplierResponse(
        String code,
        String name,
        boolean isActive
) {}
