package com.example.wish.component.impl;

import com.example.wish.component.ProfileMapper;
import com.example.wish.dto.ProfilesDetails;
import com.example.wish.dto.MainScreenProfileDto;
import com.example.wish.entity.Profile;
import com.example.wish.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProfileMapperImpl implements ProfileMapper {

    private final ModelMapper modelMapper;

    @Override
    public MainScreenProfileDto convertToDto(Profile profile) {
        MainScreenProfileDto mainScreenProfileDto = modelMapper.map(profile, MainScreenProfileDto.class);
        mainScreenProfileDto.setPhoto(ImageUtil.decompressImage(profile.getPhoto()));

        return mainScreenProfileDto;
    }

    @Override
    public ProfilesDetails convertToDtoAnother(Profile profile) {
        ProfilesDetails anotherProfileDto = modelMapper.map(profile, ProfilesDetails.class);
        anotherProfileDto.setAge(profile.getAge());
        anotherProfileDto.setPhoto(ImageUtil.decompressImage(profile.getPhoto()));
        return anotherProfileDto;
    }

}
