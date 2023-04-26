package com.example.wish.dto;

import com.example.wish.annotation.PasswordStrength;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
@ToString
public class UpdatePasswordRequest {

    private final long profileId;

    @PasswordStrength
    private final String password;

    private final String confirmPassword;
}
