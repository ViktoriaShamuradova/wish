package com.example.wish.component.impl;

import com.example.wish.component.ProfileMapper;
import com.example.wish.dto.MainScreenProfileDto;
import com.example.wish.dto.profile.ProfileDto;
import com.example.wish.dto.profile.ProfilesDetails;
import com.example.wish.entity.Profile;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Locale;

@RequiredArgsConstructor
@Component
public class ProfileMapperImpl implements ProfileMapper {

    private final ModelMapper modelMapper;

    @Override
    public ProfileDto convertToDto(Profile profile) {
        ProfileDto profileDto = modelMapper.map(profile, ProfileDto.class);
        if (profile.getCountryCode() != null) {
            String code = profile.getCountryCode().getCode().toUpperCase();
            Locale l = new Locale("", code);
            String country = l.getDisplayCountry();
            profileDto.setCountryName(country);
        }

        return profileDto;
    }

    @Override
    public MainScreenProfileDto convertToDtoMainScreen(Profile profile) {
        MainScreenProfileDto mainScreenProfileDto = modelMapper.map(profile, MainScreenProfileDto.class);

        return mainScreenProfileDto;
    }

    @Override
    public ProfilesDetails convertToDtoAnother(Profile profile) {
        ProfilesDetails anotherProfileDto = modelMapper.map(profile, ProfilesDetails.class);
        anotherProfileDto.setAge(profile.getAge());
        return anotherProfileDto;
    }

}
