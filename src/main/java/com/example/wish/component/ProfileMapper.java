package com.example.wish.component;

import com.example.wish.dto.ProfileDto;
import com.example.wish.dto.ProfilesDetails;
import com.example.wish.dto.MainScreenProfileDto;
import com.example.wish.entity.Profile;

public interface ProfileMapper {

    ProfileDto convertToDto(Profile profile);

    MainScreenProfileDto convertToDtoMainScreen(Profile profile);


    ProfilesDetails convertToDtoAnother(Profile profile);
}
