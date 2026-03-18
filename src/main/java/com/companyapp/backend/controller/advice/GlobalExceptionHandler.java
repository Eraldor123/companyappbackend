package com.companyapp.backend.controller.advice;

import com.companyapp.backend.services.exception.*;
import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 400 Bad Request ---
    @ExceptionHandler({
            AvailabilityNotProvidedException.class,
            InvalidContractLimitsException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // --- 401 Unauthorized ---
    @ExceptionHandler(InvalidPinException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedExceptions(InvalidPinException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // --- 403 Forbidden ---
    @ExceptionHandler(MissingQualificationException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenExceptions(MissingQualificationException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // --- 404 Not Found ---
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundExceptions(ResourceNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // --- 409 Conflict ---
    @ExceptionHandler({
            DuplicateResourceException.class,
            ShiftCollisionException.class,
            CapacityExceededException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictExceptions(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // --- 409 Conflict (Optimistic Locking ze Spring Data JPA / Hibernate) ---
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(OptimisticLockException ex) {
        String message = "Někdo jiný (např. jiný manažer) právě tento záznam upravil. Obnovte prosím stránku a zkuste to znovu.";
        return buildErrorResponse(HttpStatus.CONFLICT, message);
    }

    // --- 500 Internal Server Error (Záchytná síť pro neočekávané chyby) ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception ex) {
        // Zde by v praxi mělo být logování přes SLF4J: log.error("Unexpected error", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Došlo k neočekávané chybě na serveru.");
    }

    // --- Pomocná metoda pro jednotný formát JSON odpovědi ---
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String message) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    // --- DTO pro strukturu chyby (Record je dostupný od Java 14+) ---
    public record ErrorResponse(
            LocalDateTime timestamp,
            int status,
            String error,
            String message
    ) {}
}