package com.example.wish.service;

import com.example.wish.dto.MainScreenProfileDto;
import com.example.wish.dto.ProfileDto;
import com.example.wish.dto.ProfilesDetails;
import com.example.wish.dto.UpdateProfileDetails;
import com.example.wish.entity.Profile;
import com.example.wish.model.search_request.ProfileSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;


public interface ProfileService {
    MainScreenProfileDto getMainScreen();

    ProfilesDetails findByUid(String uid);

    Page<ProfilesDetails> find(Pageable pageable, ProfileSearchRequest request);

    void update(UpdateProfileDetails profileDetails) throws IOException;

    ProfileDto getProfileDto();

    void addFavoriteProfile(Long favoriteProfileId);

    List<ProfileDto> getFavoriteProfiles();

    List<ProfileDto> removeFavoriteProfile(Long favoriteProfileId);

    boolean removeProfile(String email);
}
