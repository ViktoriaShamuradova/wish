package com.example.wish.dto;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Builder
@RequiredArgsConstructor
public class AuthResponse {
    private final String type = "Bearer";

    private final String token;
}