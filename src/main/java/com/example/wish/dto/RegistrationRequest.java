package com.example.wish.dto;

import com.example.wish.annotation.FieldMatch;
import com.example.wish.annotation.PasswordStrength;
import com.example.wish.annotation.ValidEmail;
import com.example.wish.exception.FieldMatchException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@EqualsAndHashCode
@ToString
public class RegistrationRequest implements Serializable {

    @NotNull
    @ValidEmail
    private final String email;

    @NotNull
    @PasswordStrength
    private final String password;

    private final String confirmPassword;

    public RegistrationRequest(String email, String password, String confirmPassword) {
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        validatePasswordMatch();
    }

    private void validatePasswordMatch() {
        if (password != null && confirmPassword != null && !password.equals(confirmPassword)) {
            throw new FieldMatchException("The password fields must match");
        }
    }
}