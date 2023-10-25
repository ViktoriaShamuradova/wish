package com.example.wish.exception.wish;

public class ImageException extends RuntimeException {

    public ImageException(String message) {
        super(message);
    }

    public ImageException(Throwable cause) {
        super(cause);
    }

    public ImageException(String message, Throwable cause) {
        super(message, cause);
    }

}