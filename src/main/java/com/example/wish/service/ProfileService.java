package com.example.wish.service;

import com.example.wish.dto.profile.ProfileDto;
import com.example.wish.dto.profile.ProfilesDetails;
import com.example.wish.dto.profile.UpdateProfileDetails;
import com.example.wish.model.search_request.ProfileSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface ProfileService {

    ProfilesDetails findByUid(String uid);

    Page<ProfilesDetails> find(Pageable pageable, ProfileSearchRequest request);

    ProfileDto update(UpdateProfileDetails profileDetails) throws IOException;

    ProfileDto getProfileDto();

    void addFavoriteProfile(Long favoriteProfileId);

    List<ProfileDto> getFavoriteProfiles();

    List<ProfileDto> removeFavoriteProfile(Long favoriteProfileId);

    boolean removeProfile(String email);

    ProfileDto updateByFields(Map<String, Object> fields);
}
