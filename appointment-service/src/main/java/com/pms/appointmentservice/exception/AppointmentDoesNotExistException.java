package com.pms.appointmentservice.exception;

public class AppointmentDoesNotExistException extends RuntimeException {
    public AppointmentDoesNotExistException(String message) {
        super(message);
    }
}
