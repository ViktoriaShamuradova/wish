package com.example.wish.exception.wish;

public class WishException extends RuntimeException {

    public WishException(String message) {
        super(message);
    }

    public WishException(Throwable cause) {
        super(cause);
    }

    public WishException(String message, Throwable cause) {
        super(message, cause);
    }

}
