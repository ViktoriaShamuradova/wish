package com.example.wish.component;

import com.example.wish.dto.ProfilesDetails;
import com.example.wish.dto.MainScreenProfileDto;
import com.example.wish.entity.Profile;

public interface ProfileDtoBuilder {

    MainScreenProfileDto buildMainScreen(Profile profile);

    ProfilesDetails buildProfileDetails(Profile profile, boolean isCurrentProfile);
}
