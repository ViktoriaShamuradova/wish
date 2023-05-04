package com.example.wish.exception.profile;

public class ProfileNotFoundException extends ProfileException {

    public ProfileNotFoundException(String emailOrUid) {
        super("Could not find profile " + emailOrUid);
    }

    public ProfileNotFoundException(Long id) {
        super("Could not find profile " + id);
    }

}
