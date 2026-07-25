package com.mdstore.connector.vietshare.dto;

import java.util.List;

/**
 * Response của GET /v1/products (alias: GET /v1/catalog).
 * Xem docs/04-suppliers/vietshare.md — API 1.
 */
public record VsProductListResponse(
        List<VsProductItem> data
) {}
