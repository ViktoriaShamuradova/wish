package com.example.wish.dto;

import com.example.wish.annotation.FieldMatch;
import com.example.wish.annotation.PasswordStrength;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@FieldMatch(first = "password", second = "confirmPassword", message = "Passwords do not match")
@ToString
public class UpdatePasswordRequest {

    private final String email;

    @PasswordStrength
    private final String password;

    private final String confirmPassword;
}
