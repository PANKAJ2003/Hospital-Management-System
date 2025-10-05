package com.pms.appointmentservice.exception;

public class NoTimeSlotAvaliableException extends RuntimeException {
    public NoTimeSlotAvaliableException(String message) {
        super(message);
    }
}
