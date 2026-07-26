package com.mdstore.order.web;

import com.mdstore.common.web.ApiResponse;
import com.mdstore.order.service.OrderOrchestrationService;
import com.mdstore.order.web.dto.CreateOrderRequest;
import com.mdstore.order.web.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderOrchestrationService orderOrchestrationService;

    public OrderController(OrderOrchestrationService orderOrchestrationService) {
        this.orderOrchestrationService = orderOrchestrationService;
    }

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return ApiResponse.ok(orderOrchestrationService.placeOrder(request));
    }

    @GetMapping
    public ApiResponse<List<OrderResponse>> getOrders() {
        // TODO: Gọi OrderQueryService
        throw new UnsupportedOperationException("Chưa triển khai");
    }
}
