package com.example.wish.exception.profile;

public class ProfileNotFoundException extends ProfileException {

    public ProfileNotFoundException(String uid) {
        super("Could not find profile " + uid);
    }

    public ProfileNotFoundException(Long id) {
        super("Could not find profile " + id);
    }

}
