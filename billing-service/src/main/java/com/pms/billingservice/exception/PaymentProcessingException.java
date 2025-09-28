package com.pms.billingservice.exception;

public class PaymentProcessingException extends RuntimeException {

    // Constructor with only a message
    public PaymentProcessingException(String message) {
        super(message);
    }

    // Constructor with message and cause (original exception)
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    // Optional: Constructor with only cause
    public PaymentProcessingException(Throwable cause) {
        super(cause);
    }
}
