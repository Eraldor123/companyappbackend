package com.companyapp.backend.controller; // Zkontroluj si správnost balíčku

import com.companyapp.backend.services.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException; // PŘIDANÝ IMPORT PRO IDOR/ROLE
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j // PŘIDÁNO: Profesionální logování místo ex.printStackTrace()
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(x -> x.getField() + ": " + x.getDefaultMessage())
                .collect(Collectors.joining(", "));
        body.put("message", "Validace selhala: " + errors);
        return new ResponseEntity<>(body, headers, status);
    }

    // Tady jsou tvé byznys pravidla (Zůstává nezměněno, funguje to skvěle)
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
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // --- NOVÉ: ZACHYCENÍ BEZPEČNOSTNÍCH BLOKACÍ (IDOR / ROLE) ---
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        // Do logu si napíšeme skutečný důvod, proč byl uživatel zablokován
        log.warn("Bezpečnostní blokace (IDOR/Role): {}", ex.getMessage());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        // Uživateli pošleme jasnou zprávu
        body.put("message", "Přístup odepřen. K této akci nebo datům nemáte oprávnění.");
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    // NENALEZENÉ POLOŽKY TEĎ VRACÍ 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(ResourceNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // --- OPRAVENO: BEZPEČNÉ ZACHYCENÍ 500 ERRORU ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralError(Exception ex) {
        // Chybu zapíšeme celou do logu v IntelliJ pro tvoje ladění (stack trace)
        log.error("Kritická chyba na serveru: ", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());

        // Na frontend už NEPOSÍLÁME ex.getMessage(), ale pouze obecnou bezpečnou omluvu!
        body.put("message", "Omlouváme se, na serveru došlo k neočekávané chybě. Kontaktujte administrátora.");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}