package com.companyapp.backend.services.exception;

public class InvalidContractLimitsException extends RuntimeException {
    public InvalidContractLimitsException(String message) {
        super(message);
    }

    public InvalidContractLimitsException(String message, Throwable cause) {
        super(message, cause);
    }
}