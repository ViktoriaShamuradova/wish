package com.example.wish.exception.auth;


public class EmailException extends RuntimeException {

    public EmailException(String email) {
        super("Email " + email + " not valid");
    }

}