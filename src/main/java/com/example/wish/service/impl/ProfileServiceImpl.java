package com.example.wish.service.impl;

import com.example.wish.component.ProfileDtoBuilder;
import com.example.wish.dto.ProfileDto;
import com.example.wish.dto.ProfilesDetails;
import com.example.wish.dto.UpdateProfileDetails;
import com.example.wish.entity.CountryCode;
import com.example.wish.entity.Profile;
import com.example.wish.entity.Socials;
import com.example.wish.entity.meta_model.Profile_;
import com.example.wish.exception.profile.ProfileExistException;
import com.example.wish.exception.profile.ProfileNotFoundException;
import com.example.wish.model.CurrentProfile;
import com.example.wish.model.search_request.ProfileSearchRequest;
import com.example.wish.repository.ProfileRepository;
import com.example.wish.service.ProfileService;
import com.example.wish.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RequiredArgsConstructor
@Slf4j
@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileDtoBuilder profileDtoBuilder;
    private final AuthenticationService authenticationService;

    private final ProfileRepository profileRepository;


    /**
     * information about own profile for main screen
     */
    @Transactional(readOnly = true)
    @Override
    public ProfileDto getProfileDto() {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        //нужно ли это исключение, если authenticationService.getCurrentProfile уже генерит, если не находится
        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        return profileDtoBuilder.buildProfileDto(profile);
    }

    @Transactional
    @Override
    public ProfileDto update(UpdateProfileDetails updateProfileRequest) throws IOException {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        profile.setFirstname(updateProfileRequest.getFirstname());
        profile.setLastname(updateProfileRequest.getLastname());
        profile.setPhone(updateProfileRequest.getPhone());
        profile.setBirthday(updateProfileRequest.getBirthday());
        profile.setCountry(updateProfileRequest.getCountry());
        profile.setCity(updateProfileRequest.getCity());
        profile.setSex(updateProfileRequest.getSex());
        profile.setSocials(updateProfileRequest.getSocials());
        profile.setBirthday(updateProfileRequest.getBirthday());
        profile.setPhoto(updateProfileRequest.getPhoto());

        Profile saved = profileRepository.save(profile);

        return profileDtoBuilder.buildProfileDto(saved);
    }

    @Transactional
    @Override
    public ProfileDto updateByFields(Map<String, Object> fields) {

        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        fields.forEach((key, value) -> {
            if (key.equals(Profile_.SOCIALS) && value instanceof Map) {
                // Handle nested "socials" field
                Map<String, Object> socialsFields = (Map<String, Object>) value;
                Socials socials = profile.getSocials();
                if (socials == null) {
                    socials = new Socials();
                }
                Socials finalSocials = socials;
                socialsFields.forEach((socialsKey, socialsValue) -> {
                    Field socialsField = ReflectionUtils.findField(Socials.class, socialsKey);
                    if (socialsField != null) {
                        socialsField.setAccessible(true);
                        ReflectionUtils.setField(socialsField, finalSocials, socialsValue);
                    }
                });
                profile.setSocials(socials);
            } else {
                // Handle other fields
                Field field = ReflectionUtils.findField(Profile.class, key);
                assert field != null;
                field.setAccessible(true);
                if (field.getType() == Date.class && value instanceof String) {
                    // Convert String value to Date
                    try {
                        Date dateValue;
                        SimpleDateFormat dateFormat;
                        if (((String) value).matches("\\d{4}-\\d{2}-\\d{2}")) {
                            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        } else if (((String) value).matches("\\d{2}-\\d{2}-\\d{4}")) {
                            dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                        } else {
                            throw new IllegalArgumentException("Invalid date format for field: " + key);
                        }
                        dateValue = dateFormat.parse((String) value);
                        ReflectionUtils.setField(field, profile, dateValue);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Invalid date format for field: " + key);
                    }
                } else if (field.getType().isEnum()) {
                    // Check if the field is an enum of CountryCode
                        if (CountryCode.class.isAssignableFrom(field.getType())) {
                            // Convert String value to CountryCode enum
                            CountryCode enumValue = CountryCode.fromCountryName(value.toString());
                            ReflectionUtils.setField(field, profile, enumValue);
                        } else {
                            // For other enums, convert String value to the corresponding enum
                            Enum<?> enumValue = Enum.valueOf((Class<? extends Enum>) field.getType(), value.toString());
                            ReflectionUtils.setField(field, profile, enumValue);
                        }

                } else {
                    ReflectionUtils.setField(field, profile, value);
                }
            }
        });

        //else if (field.getType().isEnum()) {
        //    // Check if the field is an enum of CountryCode
        //    if (CountryCode.class.isAssignableFrom(field.getType())) {
        //        // Convert String value to CountryCode enum
        //        CountryCode enumValue = CountryCode.valueOf(value.toString());
        //        ReflectionUtils.setField(field, profile, enumValue);
        //    } else {
        //        // For other enums, convert String value to the corresponding enum
        //        Enum<?> enumValue = Enum.valueOf((Class<? extends Enum>) field.getType(), value.toString());
        //        ReflectionUtils.setField(field, profile, enumValue);
        //    }
        //} else {
        //    ReflectionUtils.setField(field, profile, value);
        //}

        Profile saved = profileRepository.save(profile);
        return profileDtoBuilder.buildProfileDto(saved);
    }


    @Override
    @Transactional
    public void addFavoriteProfile(Long favoriteProfileId) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        Profile favoriteProfile = profileRepository.findById(favoriteProfileId)
                .orElseThrow(() -> new ProfileExistException("Favorite profile not found with ID: " + favoriteProfileId));

        profile.addFavoriteProfile(favoriteProfile);
        profileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProfileDto> getFavoriteProfiles() {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        return profile.getFavoriteProfiles().stream()
                .map(profileDtoBuilder::buildProfileDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ProfileDto> removeFavoriteProfile(Long favoriteProfileId) {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        Profile favoriteProfile = profileRepository.findById(favoriteProfileId)
                .orElseThrow(() -> new ProfileNotFoundException("Favorite profile not found with ID: " + favoriteProfileId));

        profile.removeFavoriteProfile(favoriteProfile);
        profileRepository.save(profile);

        return profile.getFavoriteProfiles().stream()
                .map(profileDtoBuilder::buildProfileDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public boolean removeProfile(String email) {
        Optional<Profile> byEmail = profileRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            profileRepository.delete(byEmail.get());
            return true;
        } else {
            return false;
        }

    }


    //если тек юзер, то выводить собственные желания незавершенные
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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

