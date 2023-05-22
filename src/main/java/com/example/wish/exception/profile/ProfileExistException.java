package com.example.wish.exception.profile;

//когда генерится?
//можно использовать если не находится профиль, который ищет другой профиль
public class ProfileExistException extends ProfileException {
    public ProfileExistException(String message) {
        super(message);
    }

    public ProfileExistException(Throwable cause) {
        super(cause);
    }

    public ProfileExistException(String message, Throwable cause) {
        super(message, cause);
    }
}