package ru.neoflex.kubrak.gateway.exception;

import org.springframework.http.HttpStatus;

public class StatementServiceException extends RuntimeException {

    public StatementServiceException(String message) {
        super(message);
    }

    public StatementServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public StatementServiceException(String endpoint, HttpStatus status, String responseBody) {
        super(String.format("Statement service error. Endpoint: %s, Status: %s, Response: %s",
                endpoint, status, responseBody));
    }
}
