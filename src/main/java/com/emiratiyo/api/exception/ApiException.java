package com.emiratiyo.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception class for all custom API exceptions.
 * Sounding "Industry-Grade" by using structured custom exceptions.
 */
@Getter
public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;

    protected ApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
