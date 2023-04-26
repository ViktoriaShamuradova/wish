package com.example.wish.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Getter
@RequiredArgsConstructor
public class ProfileDetailsForPasswordDto implements Serializable {
    private final long profileId;
}
