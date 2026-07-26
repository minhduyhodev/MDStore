package com.mdstore.supplier.web;

import com.mdstore.common.web.ApiResponse;
import com.mdstore.supplier.web.dto.SupplierResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/suppliers")
public class SupplierController {

    @GetMapping
    public ApiResponse<List<SupplierResponse>> getSuppliers() {
        // TODO: Gọi SupplierQueryService
        throw new UnsupportedOperationException("Chưa triển khai");
    }
}
