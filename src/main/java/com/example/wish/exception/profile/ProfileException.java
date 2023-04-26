package com.example.wish.exception.profile;

public class ProfileException extends RuntimeException {


    public ProfileException(String message) {
        super(message);
    }

    public ProfileException(Throwable cause) {
        super(cause);
    }

    public ProfileException(String message, Throwable cause) {
        super(message, cause);
    }

}

