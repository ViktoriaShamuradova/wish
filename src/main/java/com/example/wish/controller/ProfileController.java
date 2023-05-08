package com.example.wish.controller;

import com.example.wish.dto.MainScreenProfileDto;
import com.example.wish.dto.ProfileDto;
import com.example.wish.dto.ProfilesDetails;
import com.example.wish.dto.UpdateProfileDetails;
import com.example.wish.model.search_request.ProfileSearchRequest;
import com.example.wish.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

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
    @RequestMapping(value = "/my-profile-main")
    public ResponseEntity<MainScreenProfileDto> myProfile() {
        MainScreenProfileDto mainScreenProfileDto = profileService.getMainScreen();

        return ResponseEntity.ok(mainScreenProfileDto);
    }

    /**
     * на главную страницу текущего профиля
     *
     * @return
     */
    @RequestMapping(value = "/my-profile")
    public ResponseEntity<ProfileDto> getMyProfile() {
        ProfileDto profileDto = profileService.getProfileDto();

        return ResponseEntity.ok(profileDto);
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

    /**
     * обновляем профиль
     *
     * @param profileDetails
     * @return
     * @throws IOException
     */
    @PutMapping(value = "/update")
    public ResponseEntity update(@RequestPart("profile") @Valid UpdateProfileDetails profileDetails) throws IOException {

        profileService.update(profileDetails);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}