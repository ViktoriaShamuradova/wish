package com.example.wish.service;

import com.example.wish.dto.MainScreenProfileDto;
import com.example.wish.dto.ProfilesDetails;
import com.example.wish.dto.UpdateProfileDetails;
import com.example.wish.model.search_request.ProfileSearchRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


public interface ProfileService {
    MainScreenProfileDto findMain();

    ProfilesDetails findByUid(String uid);

    Page<ProfilesDetails> find(Pageable pageable, ProfileSearchRequest request);

    void update(UpdateProfileDetails profileDetails, MultipartFile file) throws IOException;
}
