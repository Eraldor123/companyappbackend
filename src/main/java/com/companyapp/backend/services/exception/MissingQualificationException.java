package com.companyapp.backend.services.exception;

public class MissingQualificationException extends RuntimeException {
    public MissingQualificationException(String message) {
        super(message);
    }

    public MissingQualificationException(String message, Throwable cause) {
        super(message, cause);
    }
}