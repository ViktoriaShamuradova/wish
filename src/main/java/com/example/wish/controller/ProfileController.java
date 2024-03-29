package com.example.wish.controller;

import com.example.wish.dto.profile.ProfileDto;
import com.example.wish.dto.profile.ProfilesDetails;
import com.example.wish.dto.profile.UpdateProfileDetails;
import com.example.wish.model.search_request.ProfileSearchRequest;
import com.example.wish.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;


import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/demo/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;


    /**
     * на главную страницу текущего профиля
     *
     * @return
     */
    @GetMapping(value = "/my-profile")
    public ResponseEntity<ProfileDto> getMyProfile() {
        ProfileDto profileDto = profileService.getProfileDto();
        return ResponseEntity.ok(profileDto);
    }

    /**
     * обновляем профиль, нужно указывать все поля, иначе поля сохранять с нулевым значением
     * не использовать его. deprecated, exception
     *
     * @param profileDetails
     * @return
     * @throws IOException
     */
    @PutMapping(value = "/my-profile")
    public ResponseEntity<ProfileDto> update(@RequestBody @Valid UpdateProfileDetails profileDetails) throws IOException {

        ProfileDto profileDto = profileService.update(profileDetails);
        return ResponseEntity.ok(profileDto);
    }

    @PostMapping(value = "/upload-image")
    public ResponseEntity<Void> uploadImage(@RequestParam("file") @NotNull MultipartFile file,
                                            UriComponentsBuilder uriBuilder) throws IOException, HttpMediaTypeNotSupportedException {
        String uid = profileService.uploadImage(file);

        UriComponents uriComponents = uriBuilder
                .path("/v1/demo/profile/image/{uid}")
                .buildAndExpand(uid);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    /**
     * Возвращает изображение любого пользователя
     *
     * @return
     */
    @GetMapping(value = "/image/{uid}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable("uid") String uid) {
        return ResponseEntity.ok(profileService.findImage(uid));
    }

    /**
     * обновляем профиль,
     * нужно указывать те поля, которые нужно обновить
     * выбрасывается исключение, если формат даты не верный. нужно гггг-мм-дд
     *
     * @param fields
     * @return
     * @throws IOException
     */
    @PatchMapping(value = "/my-profile")
    public ResponseEntity<ProfileDto> updateByFields(@RequestBody Map<String, Object> fields) {

        ProfileDto profileDto = profileService.updateByFields(fields);
        return ResponseEntity.ok(profileDto);
    }

    /**
     * удалить можно самого себя.
     * на фронте сделать окошко с сообщением о удалении и невозможности восстановления профиля
     * очистить секьюрити холдер?
     * @return
     */
    @DeleteMapping(value = "/my-profile")
    public ResponseEntity<Void> delete() {
        profileService.delete();
        return ResponseEntity.noContent().build();
    }

    /**
     * возвращает информацию о профиле (о своем и чужом)/
     * о своем выводит собственные желания всех статусов(new, in_progress..)
     * чужого только в статусе new
     *
     * @param uid
     * @return
     */

    @GetMapping("/{uid}")
    public ResponseEntity<ProfilesDetails> getProfile(@PathVariable("uid") String uid) {
        ProfilesDetails profile = profileService.findByUid(uid);
        return ResponseEntity.ok(profile);
    }

    @PostMapping(value = "/search")
    public Page<ProfilesDetails> search(@RequestBody ProfileSearchRequest request,
                                        @PageableDefault(size = 10) @SortDefault(sort = "karma", direction = Sort.Direction.DESC) Pageable pageable) {
        return profileService.find(pageable, request);
    }


    @PostMapping("/favorite/{favoriteProfileId}")
    public ResponseEntity addFavoriteProfile(
            @PathVariable Long favoriteProfileId) {
        profileService.addFavoriteProfile(favoriteProfileId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<ProfileDto>> getFavoriteProfiles() {
        List<ProfileDto> favoriteProfiles = profileService.getFavoriteProfiles();
        return ResponseEntity.ok(favoriteProfiles);
    }


    @DeleteMapping("/favorite/{favoriteProfileId}")
    public ResponseEntity<List<ProfileDto>> removeFavoriteProfile(
            @PathVariable Long favoriteProfileId) {
        List<ProfileDto> favoriteProfiles = profileService.removeFavoriteProfile(favoriteProfileId);
        return ResponseEntity.ok(favoriteProfiles);
    }

}
