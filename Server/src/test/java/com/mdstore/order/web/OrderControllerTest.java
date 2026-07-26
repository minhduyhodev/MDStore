package com.mdstore.order.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdstore.common.web.ErrorCode;
import com.mdstore.common.web.GlobalExceptionHandler;
import com.mdstore.order.service.OrderOrchestrationService;
import com.mdstore.order.service.OrderPriceChangedException;
import com.mdstore.order.web.dto.CreateOrderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@ContextConfiguration(classes = {OrderController.class, GlobalExceptionHandler.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderOrchestrationService orderOrchestrationService;

    @Test
    void createOrder_invalidRequest_returnsBadRequestWithEnvelope() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("", 0, null); // Invalid: blank code, qty < 1

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.error.message").exists());
    }

    @Test
    void createOrder_whenSupplierPriceChanged_returnsConflictWithEnvelope() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("42", 1, null);
        when(orderOrchestrationService.placeOrder(any(CreateOrderRequest.class)))
                .thenThrow(new OrderPriceChangedException());

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.ORDER_PRICE_CHANGED.name()))
                .andExpect(jsonPath("$.error.message")
                        .value(ErrorCode.ORDER_PRICE_CHANGED.getDefaultMessage()));
    }
}
