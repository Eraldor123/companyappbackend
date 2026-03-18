package com.companyapp.backend.services.exception;

public class AvailabilityNotProvidedException extends RuntimeException {
    public AvailabilityNotProvidedException(String message) {
        super(message);
    }

    public AvailabilityNotProvidedException(String message, Throwable cause) {
        super(message, cause);
    }
}