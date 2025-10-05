package com.pms.appointmentservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PatientNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePatientNotFoundException(PatientNotFoundException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Patient not found");
        errors.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);
    }

    @ExceptionHandler(AppointmentDoesNotExistException.class)
    public ResponseEntity<Map<String, String>> handleAppointmentDoesNotExistException(AppointmentDoesNotExistException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "Appointment does not exist");
        errors.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);
    }

    @ExceptionHandler(NoTimeSlotAvaliableException.class)
    public ResponseEntity<Map<String, String>> handleNoTimeSlotAvaliableException(NoTimeSlotAvaliableException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("error", "No time slot avaliable");
        errors.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errors);
    }
}
