package com.mdstore.catalog.web.dto;

import java.math.BigDecimal;

public record CatalogItemResponse(
        String productId,
        String name,
        BigDecimal price,
        boolean inStock
) {}
