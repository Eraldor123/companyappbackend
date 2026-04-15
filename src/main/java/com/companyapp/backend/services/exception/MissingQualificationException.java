package com.companyapp.backend.services.exception;

/**
 * Výjimka vyhazovaná v momentě, kdy se plánovací algoritmus nebo administrátor
 * pokusí přiřadit zaměstnance na stanoviště, pro které nemá platnou kvalifikaci.
 */
public class MissingQualificationException extends RuntimeException {

    /**
     * OPRAVA java:S1144: Konstruktor ponechán pro budoucí validační logiku plánování.
     */
    @SuppressWarnings("unused")
    public MissingQualificationException(String message) {
        super(message);
    }

    /**
     * OPRAVA java:S1144: Konstruktor ponechán pro řetězení výjimek.
     */
    @SuppressWarnings("unused")
    public MissingQualificationException(String message, Throwable cause) {
        super(message, cause);
    }
}