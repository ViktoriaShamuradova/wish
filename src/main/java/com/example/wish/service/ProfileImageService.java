package com.example.wish.service;

import com.example.wish.entity.ProfileImage;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileImageService {
    ProfileImage save(MultipartFile image);

    void delete(ProfileImage image);
}
