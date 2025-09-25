package com.pms.billingservice.exception;

public class BillingAccountDoesNotExistException extends RuntimeException {
    public BillingAccountDoesNotExistException(String message) {
        super(message);
    }
}
