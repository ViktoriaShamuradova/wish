package com.example.wish.dto;

import com.example.wish.annotation.ValidEmail;
import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationRequest {

    @NotNull
    @ValidEmail
    private String email;

    @NotNull
    private String otp;

    @NotNull
    private boolean isRegistration;


}
