package com.example.wish.exception;

public class FieldMatchException extends RuntimeException {
    public FieldMatchException(String message) {
        super(message);
    }

    public FieldMatchException(Throwable cause) {
        super(cause);
    }

    public FieldMatchException(String message, Throwable cause) {
        super(message, cause);
    }
}