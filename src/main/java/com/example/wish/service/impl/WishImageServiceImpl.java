package com.example.wish.service.impl;

import com.example.wish.entity.WishImage;
import com.example.wish.exception.wish.WishException;
import com.example.wish.repository.WishImageRepository;
import com.example.wish.service.WishImageService;
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
public class WishImageServiceImpl implements WishImageService {

    private final WishImageRepository wishImageRepository;

    //обработать исключение код ошибки 500
    @Override
    public WishImage save(MultipartFile image) {
        WishImage wishImage = new WishImage();
        try {
            wishImage.setImageData(image.getBytes());
        } catch (IOException e) {
            throw new WishException("failed to save image");
        }

        wishImage.setCreated(Timestamp.from(Instant.now()));
        return wishImageRepository.save(wishImage);
    }

    @Override
    public void delete(WishImage image) {
        wishImageRepository.delete(image);
    }
}
