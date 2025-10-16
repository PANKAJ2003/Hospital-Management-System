package com.pms.authservice.exception;

public class UserCreationException extends RuntimeException {
    public UserCreationException(String message, Exception ex) {
        super(message);
    }
}
