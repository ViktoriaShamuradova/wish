package com.example.wish.component;

import com.example.wish.dto.AbstractWishDto;
import com.example.wish.dto.ProfileDto;
import com.example.wish.dto.ProfilesDetails;
import com.example.wish.dto.MainScreenProfileDto;
import com.example.wish.entity.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProfileDtoBuilder {

    MainScreenProfileDto buildMainScreen(Profile profile);

    ProfilesDetails buildProfileDetails(Profile profile);

    ProfileDto buildProfileDto(Profile profile);

    Page<AbstractWishDto> findOwmWishesDto(Long profileId, Pageable pageable);
}