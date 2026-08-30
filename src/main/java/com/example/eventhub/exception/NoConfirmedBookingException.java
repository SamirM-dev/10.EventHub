package com.example.eventhub.exception;

public class NoConfirmedBookingException extends RuntimeException {
    public NoConfirmedBookingException(String message) {
        super(message);
    }
}
