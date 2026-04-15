package com.companyapp.backend.services.exception;

/**
 * Výjimka vyhazovaná v situaci, kdy algoritmus plánování narazí na chybějící
 * data o dostupnosti zaměstnance.
 */
public class AvailabilityNotProvidedException extends RuntimeException {

    public AvailabilityNotProvidedException(String message) {
        super(message);
    }

    /**
     * OPRAVA java:S1144: Konstruktor je ponechán pro budoucí "Exception Chaining".
     * Označeno SuppressWarnings, aby statická analýza nezastavovala build
     * kvůli standardnímu architektonickému vzoru.
     */
    @SuppressWarnings("unused")
    public AvailabilityNotProvidedException(String message, Throwable cause) {
        super(message, cause);
    }
}