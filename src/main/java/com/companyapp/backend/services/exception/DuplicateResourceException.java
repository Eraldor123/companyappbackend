package com.companyapp.backend.services.exception;

/**
 * Výjimka vyhazovaná při pokusu o vytvoření entity, která by porušila
 * unikátní omezení v databázi (např. duplicitní e-mail nebo login).
 */
public class DuplicateResourceException extends RuntimeException {

    // OPRAVA: Odstraněno slovo "Exception" před názvem konstruktoru
    public DuplicateResourceException(String message) {
        super(message);
    }

    /**
     * Konstruktor ponechán pro řetězení výjimek (Exception Chaining).
     */
    @SuppressWarnings("unused")
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}