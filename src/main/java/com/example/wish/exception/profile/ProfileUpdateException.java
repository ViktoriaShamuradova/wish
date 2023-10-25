package com.example.wish.exception.profile;

public class ProfileUpdateException extends ProfileException {

    public ProfileUpdateException(String message) {
        super("profile update error. this field cannot be changed or is missing from the profile - " + message);
    }



}
