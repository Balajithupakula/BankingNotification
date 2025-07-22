package com.example.notification;

public class CustomException extends RuntimeException {
    public CustomException(String message) {
        super(message);
    }
}