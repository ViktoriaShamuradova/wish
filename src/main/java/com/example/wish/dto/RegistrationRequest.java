package com.example.wish.dto;

import com.example.wish.annotation.PasswordStrength;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
@ToString
//@FieldMatch(first = "password", second = "confirmPassword", message = "The password fields must match")
public class RegistrationRequest implements Serializable {

    private final String email;

    @PasswordStrength
    private final String password;

    private final String confirmPassword;
}