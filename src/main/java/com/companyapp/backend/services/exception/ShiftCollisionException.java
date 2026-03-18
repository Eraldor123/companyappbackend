package com.companyapp.backend.services.exception;

public class ShiftCollisionException extends RuntimeException {
    public ShiftCollisionException(String message) {
        super(message);
    }

    public ShiftCollisionException(String message, Throwable cause) {
        super(message, cause);
    }
}