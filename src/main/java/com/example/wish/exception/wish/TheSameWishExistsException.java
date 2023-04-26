package com.example.wish.exception.wish;

public class TheSameWishExistsException extends WishException {
    public TheSameWishExistsException(String message) {
        super(message);
    }

    public TheSameWishExistsException(Throwable cause) {
        super(cause);
    }

    public TheSameWishExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
