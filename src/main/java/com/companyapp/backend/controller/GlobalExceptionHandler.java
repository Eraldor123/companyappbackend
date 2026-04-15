package com.companyapp.backend.controller;

import com.companyapp.backend.services.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // OPRAVA java:S1192: Definice konstant pro opakující se textové klíče v JSON odpovědích.
    // Tím se odstraňují duplikované literály a usnadňuje budoucí údržba kódu.
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String STATUS_KEY = "status";
    private static final String MESSAGE_KEY = "message";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP_KEY, LocalDateTime.now());
        body.put(STATUS_KEY, HttpStatus.BAD_REQUEST.value());

        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(x -> x.getField() + ": " + x.getDefaultMessage())
                .collect(Collectors.joining(", "));

        body.put(MESSAGE_KEY, "Validace selhala: " + errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            CapacityExceededException.class,
            AvailabilityNotProvidedException.class,
            ShiftCollisionException.class,
            DuplicateResourceException.class,
            MissingQualificationException.class,
            InvalidContractLimitsException.class
    })
    public ResponseEntity<Object> handleBusinessRules(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP_KEY, LocalDateTime.now());
        body.put(STATUS_KEY, HttpStatus.BAD_REQUEST.value());
        body.put(MESSAGE_KEY, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Bezpečnostní blokace (IDOR/Role): {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP_KEY, LocalDateTime.now());
        body.put(STATUS_KEY, HttpStatus.FORBIDDEN.value());
        body.put(MESSAGE_KEY, "Přístup odepřen. K této akci nebo datům nemáte oprávnění.");
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP_KEY, LocalDateTime.now());
        body.put(STATUS_KEY, HttpStatus.NOT_FOUND.value());
        body.put(MESSAGE_KEY, ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    @SuppressWarnings("java:S1181")
    public ResponseEntity<Object> handleGeneralError(Exception ex) {
        log.error("Kritická chyba na serveru: ", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put(TIMESTAMP_KEY, LocalDateTime.now());
        body.put(STATUS_KEY, HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put(MESSAGE_KEY, "Omlouváme se, na serveru došlo k neočekávané chybě. Kontaktujte administrátora.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}