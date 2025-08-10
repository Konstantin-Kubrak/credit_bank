package ru.neoflex.kubrak.gateway.exception;

import org.springframework.http.HttpStatus;

public class DealServiceException extends RuntimeException {

    public DealServiceException(String message) {
        super(message);
    }

    public DealServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DealServiceException(String endpoint, HttpStatus status, String responseBody) {
        super(String.format("Deal service error. Endpoint: %s, Status: %s, Response: %s",
                endpoint, status, responseBody));
    }
}
