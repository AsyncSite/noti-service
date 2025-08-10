package com.asyncsite.notiservice.adapter.in.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorDetail error,
        LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, LocalDateTime.now());
    }
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorDetail(code, message, null), LocalDateTime.now());
    }
}

record ErrorDetail(
        String code,
        String message,
        Map<String, Object> details
) {
};
