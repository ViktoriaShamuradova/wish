package com.example.wish.exception.wish;

public class WishNotFoundException extends WishException{
    public WishNotFoundException(Long id) {
        super("wish not found by id = " + id);
    }

    public WishNotFoundException(Throwable cause) {
        super(cause);
    }

    public WishNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
