package com.mdstore.catalog.web;

import com.mdstore.catalog.web.dto.CatalogItemResponse;
import com.mdstore.catalog.web.dto.CatalogListResponse;
import com.mdstore.common.web.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    @GetMapping
    public ApiResponse<CatalogListResponse> getCatalog() {
        // TODO: Gọi CatalogSyncService (hoặc CatalogQueryService)
        // Tạm thời trả về Not Implemented
        throw new UnsupportedOperationException("Chưa triển khai");
    }

    @GetMapping("/{id}")
    public ApiResponse<CatalogItemResponse> getCatalogItem(@PathVariable String id) {
        // TODO: Gọi Service
        throw new UnsupportedOperationException("Chưa triển khai");
    }
}
