package com.example.wish.exception.profile;

public class ProfileUpdateException extends ProfileException {

    public ProfileUpdateException(String message) {
        super("This field does not exist in the profile - " + message);
    }



}
