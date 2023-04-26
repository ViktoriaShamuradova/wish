package com.example.wish.exception.auth;

public class TokenException extends RuntimeException {
    public TokenException(String message) {
        super(message);
    }

    public TokenException(Throwable cause) {
        super(cause);
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
