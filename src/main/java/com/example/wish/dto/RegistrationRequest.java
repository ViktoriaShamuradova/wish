package com.example.wish.dto;

import com.example.wish.annotation.FieldMatch;
import com.example.wish.annotation.PasswordStrength;
import com.example.wish.annotation.ValidEmail;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@EqualsAndHashCode
@ToString
@FieldMatch(first = "password", second = "confirmPassword", message = "Passwords do not match")
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequest implements Serializable {

    @NotNull
    @ValidEmail
    private String email;

    @NotNull
    @PasswordStrength
    private String password;

    @NotNull
    private String confirmPassword;

}