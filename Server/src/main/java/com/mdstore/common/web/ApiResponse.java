package com.mdstore.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;

/**
 * Response envelope chuẩn cho mọi API backend → frontend (ADR-007).
 *
 * Thành công: { success: true, data: {...}, meta: { timestamp: "..." } }
 * Lỗi:       { success: false, error: { code, message } }
 *
 * KHÔNG trả raw supplier response thẳng từ controller — dùng factory method bên dưới.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        Meta meta,
        ErrorBody error
) {

    public record Meta(String timestamp) {
        public static Meta now() {
            return new Meta(Instant.now().toString());
        }
    }

    public record ErrorBody(String code, String message) {}

    // --- Factory methods ---

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, Meta.now(), null);
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, null, new ErrorBody(code, message));
    }
}
