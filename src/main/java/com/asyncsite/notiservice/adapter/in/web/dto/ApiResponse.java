package com.asyncsite.notiservice.adapter.in.web.dto;

import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorDetail error,
        LocalDateTime timestamp
) {
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(new ApiResponse<>(true, data, null, LocalDateTime.now()));
    }
    public static <T> ResponseEntity<ApiResponse<T>> error(String code, String message) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(false, null, new ErrorDetail(code, message, null), LocalDateTime.now()));
    }
}

record ErrorDetail(
        String code,
        String message,
        Map<String, Object> details
) {
};
