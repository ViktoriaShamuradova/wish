package com.example.wish.dto;

import lombok.*;

@Data
@Builder
@RequiredArgsConstructor
public class AuthResponse {
    private final String type = "Bearer";

    private final String accessToken;
    private final String refreshToken;

}