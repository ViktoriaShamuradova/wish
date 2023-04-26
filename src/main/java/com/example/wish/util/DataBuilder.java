package com.example.wish.util;

import com.example.wish.exception.profile.ProfileException;
import org.springframework.stereotype.Component;

@Component
public class DataBuilder {

    private static final String UID_DELIMETER = "-";

    public static String buildProfileUid(String firstName, String lastName) {
        return DataUtil.normalizeName(firstName) + UID_DELIMETER + DataUtil.normalizeName(lastName);
    }

    public static String buildProfileUid(String email) {
        int index = email.indexOf("@");
        String username;
        if (index != -1) {
            username = email.substring(0, index);
        } else {
            throw new ProfileException("invalid email");
        }
        return DataUtil.normalizeName(username);
    }

    public static String rebuildUidWithRandomSuffix(String baseUid, String alphabet, int letterCount) {
        return baseUid + UID_DELIMETER + DataUtil.generateRandomString(alphabet, letterCount);
    }

}