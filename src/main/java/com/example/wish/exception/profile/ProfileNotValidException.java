package com.example.wish.exception.profile;

public class ProfileNotValidException extends ProfileException {

    public ProfileNotValidException(String uid) {
        super("Could not find profile " + uid);
    }

    public ProfileNotValidException(Long id) {
        super("Could not find profile " + id);
    }

}