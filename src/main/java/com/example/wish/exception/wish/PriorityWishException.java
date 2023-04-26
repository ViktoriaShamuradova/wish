package com.example.wish.exception.wish;


import com.example.wish.exception.wish.WishException;

public class PriorityWishException extends WishException {
    public PriorityWishException(String message) {
        super(message);
    }

    public PriorityWishException(Throwable cause) {
        super(cause);
    }

    public PriorityWishException(String message, Throwable cause) {
        super(message, cause);
    }
}
