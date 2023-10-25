package com.example.wish.service.impl;

import com.example.wish.entity.ProfileImage;
import com.example.wish.exception.wish.ImageException;
import com.example.wish.exception.wish.WishException;
import com.example.wish.repository.ProfileImageRepository;
import com.example.wish.service.ProfileImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;

@RequiredArgsConstructor
@Slf4j
@Service
public class ProfileImageServiceImpl implements ProfileImageService {
    private final ProfileImageRepository profileImageRepository;

    //обработать исключение код ошибки 500
    @Override
    public ProfileImage save(MultipartFile image) {
        ProfileImage profileImage = new ProfileImage();
        try {
            profileImage.setImageData(image.getBytes());
        } catch (IOException e) {
            throw new ImageException("failed to save image");
        }

        profileImage.setCreated(Timestamp.from(Instant.now()));
        return profileImageRepository.save(profileImage);
    }

    @Override
    public void delete(ProfileImage image) {
        profileImageRepository.delete(image);
    }
}
