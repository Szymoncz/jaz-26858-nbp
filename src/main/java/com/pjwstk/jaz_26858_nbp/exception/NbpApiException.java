package com.pjwstk.jaz_26858_nbp.exception;

import org.springframework.http.HttpStatus;

public class NbpApiException extends RuntimeException {
    private final HttpStatus status;

    public NbpApiException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}