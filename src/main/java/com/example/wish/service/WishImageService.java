package com.example.wish.service;

import com.example.wish.entity.WishImage;
import org.springframework.web.multipart.MultipartFile;


public interface WishImageService {

    WishImage save(MultipartFile image);

    void delete(WishImage image);
}
