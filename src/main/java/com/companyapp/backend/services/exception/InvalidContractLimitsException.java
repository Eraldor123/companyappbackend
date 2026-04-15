package com.companyapp.backend.services.exception;

/**
 * Výjimka vyhazovaná v případě, že plánování nebo docházka poruší
 * limity definované v pracovní smlouvě (např. maximální počet hodin).
 */
public class InvalidContractLimitsException extends RuntimeException {

    /**
     * OPRAVA java:S1144: Konstruktor ponechán pro budoucí validační logiku úvazků.
     */
    @SuppressWarnings("unused")
    public InvalidContractLimitsException(String message) {
        super(message);
    }

    /**
     * OPRAVA java:S1144: Konstruktor ponechán pro řetězení výjimek.
     */
    @SuppressWarnings("unused")
    public InvalidContractLimitsException(String message, Throwable cause) {
        super(message, cause);
    }
}