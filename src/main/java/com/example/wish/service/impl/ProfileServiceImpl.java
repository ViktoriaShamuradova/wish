package com.example.wish.service.impl;

import com.example.wish.component.ProfileDtoBuilder;
import com.example.wish.dto.MainScreenProfileDto;
import com.example.wish.dto.ProfileDto;
import com.example.wish.dto.ProfilesDetails;
import com.example.wish.dto.UpdateProfileDetails;
import com.example.wish.entity.Profile;
import com.example.wish.exception.profile.ProfileNotFoundException;
import com.example.wish.model.CurrentProfile;
import com.example.wish.model.search_request.ProfileSearchRequest;
import com.example.wish.repository.ProfileRepository;
import com.example.wish.service.ProfileService;
import com.example.wish.util.DataUtil;
import com.example.wish.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileDtoBuilder profileDtoBuilder;
    private final AuthenticationService authenticationService;

    private final ProfileRepository profileRepository;

    @Override
    public MainScreenProfileDto getMainScreen() {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        return profileDtoBuilder.buildMainScreen(profileRepository.findById(currentProfile.getId()).get());
    }

    /**
     * information about own profile for main screen
     */
    @Override
    public ProfileDto getProfileDto() {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        return profileDtoBuilder.buildProfileDto(profile);
    }


    //если тек юзер, то выводить собственные желания незавершенные
    @Override
    public ProfilesDetails findByUid(String uid) {
        log.info("find profile {} ", uid);

        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findByUid(uid).orElseThrow(() -> new ProfileNotFoundException(uid));

        if (currentProfile.getId() == profile.getId()) {
            return profileDtoBuilder.buildProfileDetails(profile);
        } else {
            return profileDtoBuilder.buildProfileDetails(profile);
        }
    }

    /**
     * Если заданы параметры реквеста для желания, то отобажать профили только те,
     * у кого есть такие желания со статусом new и используем join в репозитории.
     * И устанавливаем флаг в реквесте true, что параметры для желания есть. - это нужно, чтобы использовать один
     * join на все predicates
     * Иначе ищем профили по параметрам из реквеста только для профилей без объединения таблиц в репозитории.
     * Строим AnotherProfileDto только с new желаниями
     *
     * @param pageable
     * @param request
     * @return
     */
    @Override
    public Page<ProfilesDetails> find(Pageable pageable, ProfileSearchRequest request) {
        //setWishStatus(request);
        setProfileNameInRequest(request);
        setProfileBirthday(request);

        Page<Profile> all = profileRepository.findAll(ProfileRepository.Specs.bySearchRequest(request), pageable);

        Stream<ProfilesDetails> searchAnotherProfileStream = all.getContent().stream()
                .map(profileDtoBuilder::buildProfileDetails);

        return new PageImpl<>(searchAnotherProfileStream.collect(Collectors.toList()), pageable, all.getTotalElements());
    }

    @Override
    public void update(UpdateProfileDetails updateProfileRequest) throws IOException {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        String[] nameParts = {"", ""};
        if (updateProfileRequest.getName() != null && !updateProfileRequest.getName().isEmpty()) {
            nameParts = updateProfileRequest.getName().trim().split(" ");
        }
        profile.setLastname(DataUtil.capitalizeName(nameParts[0]));

        if (nameParts.length > 1) {
            profile.setFirstname(DataUtil.capitalizeName(nameParts[1]));
        }
        profile.setPhone(updateProfileRequest.getPhone());
        profile.setBirthday(updateProfileRequest.getBirthday());
        profile.setCountry(updateProfileRequest.getCountry());
        profile.setCity(updateProfileRequest.getCity());
        profile.setSex(updateProfileRequest.getSex());
        profile.setSocials(updateProfileRequest.getSocials());
        profile.setBirthday(updateProfileRequest.getBirthday());
        profile.setPhoto(updateProfileRequest.getPhoto());

        profileRepository.save(profile);
    }


    private void setProfileBirthday(ProfileSearchRequest request) {
        if (request.getMinAge() != null && request.getMaxAge() == null) {
            request.setFromDate(DateUtil.getDate(100));
            request.setToDate(DateUtil.getDate(request.getMinAge()));
        }
        if (request.getMinAge() == null && request.getMaxAge() != null) {
            request.setFromDate(DateUtil.getDate(request.getMaxAge()));
            request.setToDate(DateUtil.getDate(10));
        }
        if (request.getMinAge() != null && request.getMaxAge() != null) {
            request.setFromDate(DateUtil.getDate(request.getMaxAge()));
            request.setToDate(DateUtil.getDate(request.getMinAge()));
        }
    }

    private void setProfileNameInRequest(ProfileSearchRequest request) {
        if (request.getFirstnameLastname() != null && !request.getFirstnameLastname().isEmpty()) {
            String firstnameLastname = request.getFirstnameLastname().trim();
            String[] parts = firstnameLastname.split("\\s+");
            request.setLastname(parts[0]);
            if (parts.length > 1) {
                request.setFirstname(parts[1]);
            }
        }
    }

}

//   private void setWishStatus(ProfileSearchRequest request) {
//        if ((request.getTags() != null && !request.getTags().isEmpty())) {
//            request.setWishStatus(WishStatus.NEW);
//            request.setParametersForWish(true);
//        }
//    }
