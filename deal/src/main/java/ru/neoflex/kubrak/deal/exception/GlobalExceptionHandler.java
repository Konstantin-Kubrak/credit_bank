package ru.neoflex.kubrak.deal.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CalculatorServiceException.class)
    public ResponseEntity<?> handleCalculatorServiceException(CalculatorServiceException ex) {

        log.error("Calculator service error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(CreditRequestFailedException.class)
    public ResponseEntity<?> handleCreditRequestFailedException(CreditRequestFailedException ex) {

        log.error("Credit request failed", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    @ExceptionHandler(StatementNotFoundException.class)
    public ResponseEntity<?> handleStatementNotFoundException(StatementNotFoundException ex) {

        log.error("Statement not found", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
