package com.emiratiyo.api.exception;

import org.springframework.http.HttpStatus;

public class EmailDispatchException extends ApiException {
    public EmailDispatchException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public EmailDispatchException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
