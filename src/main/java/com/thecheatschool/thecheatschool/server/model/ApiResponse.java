package com.thecheatschool.thecheatschool.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@AllArgsConstructor
@Schema(description = "Standard API response wrapper for all endpoints")
public class ApiResponse<T> {

    private String status;
    private T data;
    private String message;

    public ApiResponse(String status, T data) {
        this.status = status;
        this.data = data;
        this.message = null;
    }
}