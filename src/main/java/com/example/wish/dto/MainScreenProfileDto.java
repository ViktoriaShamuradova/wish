package com.example.wish.dto;

import com.example.wish.entity.ProfileStatus;
import com.example.wish.entity.ProfileStatusLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 */

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MainScreenProfileDto {
    private long id;
    private String uid;
    private String firstName;
    private String lastName;
    private double karma;
    private ProfileStatus status;
    private ProfileStatusLevel statusLevel;
    private byte[] photo;

    private List<AbstractWishDto> ownWishes;
    private List<ExecutingWishDto> otherWishesInProgress;
    private List<EnergyPracticeDto> energyPractices;
}