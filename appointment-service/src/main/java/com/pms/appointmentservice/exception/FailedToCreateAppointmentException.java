package com.pms.appointmentservice.exception;

public class FailedToCreateAppointmentException extends RuntimeException {
    public FailedToCreateAppointmentException(String message) {
        super(message);
    }
}
