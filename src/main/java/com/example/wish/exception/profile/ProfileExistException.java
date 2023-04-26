package com.example.wish.exception.profile;

public class ProfileExistException extends ProfileException {
    public ProfileExistException(String message) {
        super(message);
    }

    public ProfileExistException(Throwable cause) {
        super(cause);
    }

    public ProfileExistException(String message, Throwable cause) {
        super(message, cause);
    }
}