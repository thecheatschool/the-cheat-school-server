package com.emiratiyo.api.dto;

import lombok.Builder;

@Builder
public record ApiResponse<T>(
    String status,
    T data,
    String message
) {
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .status("error")
                .message(message)
                .build();
    }
}
