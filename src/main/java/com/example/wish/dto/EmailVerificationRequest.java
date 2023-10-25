package com.example.wish.dto;

import com.example.wish.annotation.ValidEmail;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("isRegistration")
    private boolean isRegistration;

}
