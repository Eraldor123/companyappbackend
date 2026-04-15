package com.companyapp.backend.services.exception;

/**
 * Výjimka vyhazovaná při neúspěšné autentizaci uživatele pomocí PIN kódu,
 * typicky na docházkovém terminálu.
 */
public class InvalidPinException extends RuntimeException {

    public InvalidPinException(String message) {
        super(message);
    }

    /**
     * OPRAVA java:S1144: Konstruktor ponechán pro budoucí "Exception Chaining".
     * Může se hodit, pokud budeme chtít zabalit nízkoúrovňovou chybu šifrování
     * nebo databáze do srozumitelné doménové výjimky.
     */
    @SuppressWarnings("unused")
    public InvalidPinException(String message, Throwable cause) {
        super(message, cause);
    }
}