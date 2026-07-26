package com.mdstore.order.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdstore.common.web.ErrorCode;
import com.mdstore.common.web.GlobalExceptionHandler;
import com.mdstore.order.web.dto.CreateOrderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

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
    void createOrder_notImplemented_returnsInternalServerErrorWithEnvelope() throws Exception {
        CreateOrderRequest request = new CreateOrderRequest("VS_NORDVPN_1M", 1, null);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.INTERNAL_SERVER_ERROR.name()));
    }
}
