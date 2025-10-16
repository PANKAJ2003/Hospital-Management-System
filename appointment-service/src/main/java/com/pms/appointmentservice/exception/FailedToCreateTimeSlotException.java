package com.pms.appointmentservice.exception;

public class FailedToCreateTimeSlotException extends RuntimeException {
  public FailedToCreateTimeSlotException(String message) {
    super(message);
  }
}
