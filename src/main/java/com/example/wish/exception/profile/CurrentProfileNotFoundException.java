package com.example.wish.exception.profile;

public class CurrentProfileNotFoundException extends RuntimeException {

    public CurrentProfileNotFoundException(String message) {
        super(message);
    }

    public CurrentProfileNotFoundException(Throwable cause) {
        super(cause);
    }

    public CurrentProfileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
