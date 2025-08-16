package com.rinhadebackend.model;

import org.springframework.http.HttpStatus;

public class ProcessorResponse {
    private String message;
    private HttpStatus statusCode;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }

    public boolean is5xxServerError() {
        return statusCode.is5xxServerError();
    }
}
