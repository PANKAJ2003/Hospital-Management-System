package com.pms.authservice;

public class UserCreationException extends RuntimeException {
    public UserCreationException(String message, Exception ex) {
        super(message);
    }
}
