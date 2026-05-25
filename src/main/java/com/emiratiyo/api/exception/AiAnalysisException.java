package com.emiratiyo.api.exception;

import org.springframework.http.HttpStatus;

public class AiAnalysisException extends ApiException {
    public AiAnalysisException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }

    public AiAnalysisException(String message, Throwable cause) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE);
    }
}
