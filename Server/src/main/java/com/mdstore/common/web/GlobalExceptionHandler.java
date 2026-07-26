package com.mdstore.common.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Bắt mọi exception và trả về ApiResponse theo ADR-007.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleApiException(ApiException ex) {
        log.warn("API Exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        return ApiResponse.error(ex.getErrorCode().name(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMsg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        
        log.warn("Validation failed: {}", errorMsg);
        return ApiResponse.error(ErrorCode.BAD_REQUEST.name(), errorMsg);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleGeneralException(Exception ex) {
        log.error("Unhandled Exception: ", ex);
        return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR.name(), ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage());
    }
}
