package com.example.wish.exception.profile;

/**
 * убедиться, что это исключение используется только для верификации подлинности текущего юзера для изменения своих данных
 * и вхоода в личный аккаунт
 */
public class ProfileNotFoundException extends ProfileException {

    public ProfileNotFoundException(String emailOrUid) {
        super("Could not find profile " + emailOrUid);
    }

    public ProfileNotFoundException(Long id) {
        super("Could not find profile " + id);
    }

}
