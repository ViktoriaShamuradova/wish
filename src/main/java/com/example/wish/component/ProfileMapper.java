package com.example.wish.component;

import com.example.wish.dto.ProfilesDetails;
import com.example.wish.dto.MainScreenProfileDto;
import com.example.wish.entity.Profile;

public interface ProfileMapper {

    MainScreenProfileDto convertToDto(Profile profile);

    ProfilesDetails convertToDtoAnother(Profile profile);
}
