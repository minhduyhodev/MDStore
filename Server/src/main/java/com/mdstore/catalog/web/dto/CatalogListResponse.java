package com.mdstore.catalog.web.dto;

import java.util.List;

public record CatalogListResponse(
        List<CatalogItemResponse> items
) {}
