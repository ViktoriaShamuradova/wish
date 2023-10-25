package com.example.wish.component;

import com.example.wish.dto.wish.AbstractWishDto;
import com.example.wish.dto.profile.ProfileDto;
import com.example.wish.dto.profile.ProfilesDetails;
import com.example.wish.dto.MainScreenProfileDto;
import com.example.wish.entity.Profile;

import java.util.List;

public interface ProfileDtoBuilder {

    MainScreenProfileDto buildMainScreen(Profile profile);

    ProfilesDetails buildProfileDetails(Profile profile);

    ProfileDto buildProfileDto(Profile profile);

    List<AbstractWishDto> findOwmWishesDto(Long profileId);
}
