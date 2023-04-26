package com.example.wish.exception.wish;

import com.example.wish.exception.wish.WishException;

public class MaxCountOfWishesException extends WishException {
    public MaxCountOfWishesException(String message) {
        super(message);
    }

    public MaxCountOfWishesException(Throwable cause) {
        super(cause);
    }

    public MaxCountOfWishesException(String message, Throwable cause) {
        super(message, cause);
    }
}
