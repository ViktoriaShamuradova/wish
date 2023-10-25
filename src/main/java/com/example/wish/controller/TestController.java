package com.example.wish.controller;

import com.example.wish.dto.wish.WishImageDto;
import com.example.wish.service.ProfileService;
import com.example.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/demo/test")
@RequiredArgsConstructor
public class TestController {

    private final ProfileService profileService;
    private final WishService wishService;

    @DeleteMapping("/delete/{email}")
    public ResponseEntity removeFavoriteProfile(
            @PathVariable String email) {
        boolean isDeleted = profileService.removeProfile(email);
        return new ResponseEntity<>(isDeleted, HttpStatus.I_AM_A_TEAPOT);
    }

    //качество без потерь и прозрачность - PNG .
    // меньшие размеры файлов и более быстрая загрузка - JPEG.
    @GetMapping(path = "/image/{id}",  produces = MediaType.IMAGE_PNG_VALUE)
    public @ResponseBody byte[] getImage(@PathVariable("id") long wishId) {
       // return ResponseEntity.ok(wishService.findImage(wishId));
        return wishService.findImage(wishId);
    }

}
