package com.mdstore.common.web;

import com.mdstore.order.service.OrderOutOfStockException;
import com.mdstore.order.service.OrderPriceChangedException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleApiException_whenOrderPriceChanged_returnsConflictEnvelope() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(
                new OrderPriceChangedException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isFalse();
        assertThat(response.getBody().error().code()).isEqualTo("ORDER_PRICE_CHANGED");
        assertThat(response.getBody().error().message())
                .isEqualTo("Giá sản phẩm đã thay đổi, vui lòng xác nhận lại");
    }

    @Test
    void handleApiException_whenOrderOutOfStock_returnsConflictEnvelope() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(
                new OrderOutOfStockException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("ORDER_OUT_OF_STOCK");
    }

    @Test
    void handleApiException_whenSupplierFails_returnsBadGatewayWithoutSupplierDetails() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(
                new ApiException(ErrorCode.SUPPLIER_ERROR));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().message())
                .isEqualTo(ErrorCode.SUPPLIER_ERROR.getDefaultMessage());
    }

    @Test
    void handleApiException_whenGenericBadRequest_keepsBadRequestStatus() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleApiException(
                new ApiException(ErrorCode.BAD_REQUEST));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code()).isEqualTo("BAD_REQUEST");
    }
}
