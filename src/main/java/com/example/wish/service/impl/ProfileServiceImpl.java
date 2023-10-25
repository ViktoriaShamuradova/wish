package com.example.wish.service.impl;

import com.example.wish.component.ProfileDtoBuilder;
import com.example.wish.dto.profile.ProfileDto;
import com.example.wish.dto.profile.ProfilesDetails;
import com.example.wish.dto.profile.UpdateProfileDetails;
import com.example.wish.entity.*;
import com.example.wish.entity.meta_model.Profile_;
import com.example.wish.exception.profile.ProfileException;
import com.example.wish.exception.profile.ProfileExistException;
import com.example.wish.exception.profile.ProfileNotFoundException;
import com.example.wish.exception.profile.ProfileUpdateException;
import com.example.wish.model.CurrentProfile;
import com.example.wish.model.search_request.ProfileSearchRequest;
import com.example.wish.repository.ProfileRepository;
import com.example.wish.service.ProfileImageService;
import com.example.wish.service.ProfileService;
import com.example.wish.service.WishService;
import com.example.wish.util.DateUtil;
import com.example.wish.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MultipartFile;

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

    private final WishService wishService;


    private final ProfileRepository profileRepository;
    private final ProfileImageService profileImageService;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxSize;


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

        profile.setFirstName(updateProfileRequest.getFirstName());
        profile.setLastName(updateProfileRequest.getLastName());
        profile.setPhone(updateProfileRequest.getPhone());
        profile.setBirthday(updateProfileRequest.getBirthday());
        profile.setCountryCode(updateProfileRequest.getCountryCode());
        profile.setCity(updateProfileRequest.getCity());
        profile.setSex(updateProfileRequest.getSex());
        profile.setSocials(updateProfileRequest.getSocials());
        profile.setBirthday(updateProfileRequest.getBirthday());

        Profile saved = profileRepository.save(profile);

        return profileDtoBuilder.buildProfileDto(saved);
    }

    @Transactional
    @Override
    public ProfileDto updateByFields(Map<String, Object> fields) {

        CurrentProfile currentProfile = authenticationService.getCurrentProfile();
        Profile profile = profileRepository.findById(currentProfile.getId())
                .orElseThrow(() -> new ProfileNotFoundException(currentProfile.getId()));

        //сверить, что обновляем только те поля, которые можно
        checkFieldsToUpdate(fields, profile);

        fields.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Profile.class, key);
            assert field != null;
            field.setAccessible(true);

            if (key.equals(Profile_.SOCIALS) && value instanceof Map) {
                profile.setSocials(identifyProfileSocials(profile.getSocials(), value));
            } else if (field.getType() == Date.class && value instanceof String) {
                try {
                    setProfileDate(field, profile, key, value);
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Invalid date format for field: " + key);
                }
            } else if (field.getType().isEnum()) {
                setEnumField(field, profile, value.toString());
            } else {
                ReflectionUtils.setField(field, profile, value);
            }
        });
        return profileDtoBuilder.buildProfileDto(profileRepository.save(profile));
    }

    private void setEnumField(Field field, Profile profile, String value) {
        try {
            if (field.getType().isEnum()) {
                Enum<?> enumValue;

                if (field.getType().equals(CountryCode.class)) {
                    enumValue = CountryCode.fromCountryName(value);
                } else if (field.getType().equals(Sex.class)) {
                    enumValue = Sex.fromString(value);
                } else {
                    // For other enums, convert String value to the corresponding enum
                    enumValue = Enum.valueOf((Class<? extends Enum>) field.getType(), value);
                }
                ReflectionUtils.setField(field, profile, enumValue);
            } else {
                throw new IllegalArgumentException("Field is not of enum type: " + field.getName());
            }
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid enum value for field: " + field.getName());
        }
    }

    /**
     * можно удалить аккаунт, если он исполняет желания
     * нельзя удалить аккаунт, если его желания исполняют
     * <p>
     * удалить желания из таблицы executing wishes, поменять статус желания на new
     */
    @Transactional
    @Override
    public void delete() {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        Optional<Profile> byId = profileRepository.findById(currentProfile.getId());

        if (!wishService.getOwnWish(WishStatus.IN_PROGRESS).isEmpty())
            throw new ProfileException("can't delete profile. Profile have wishes in_progress");

        wishService.deleteExecutingWishes(byId.get());
        profileRepository.delete(byId.get());

    }

    /**
     * @param file
     * @return image id
     * @throws HttpMediaTypeNotSupportedException
     * @throws IOException
     */
    @Transactional
    @Override
    public String uploadImage(MultipartFile file) throws HttpMediaTypeNotSupportedException, IOException {
        CurrentProfile currentProfile = authenticationService.getCurrentProfile();

        ImageUtil.checkImage(file, maxSize);
        Optional<Profile> optionalProfile = profileRepository.findById(currentProfile.getId());

        if (optionalProfile.get().getImage() != null) {
            profileImageService.delete(optionalProfile.get().getImage());
        }

        optionalProfile.get().setImage(profileImageService.save(file));
        return optionalProfile.get().getUid();
    }

    @Transactional(readOnly = true)
    @Override
    public byte[] findImage(String uid) {
        Profile profile = profileRepository.findByUid(uid)
                .orElseThrow(() -> new ProfileNotFoundException(uid));

        if (profile.getImage() == null) return null;
        return profile.getImage().getImageData();
    }

    private void setProfileEnum(Field field, Profile profile, Enum<?> enumValue) {
        ReflectionUtils.setField(field, profile, enumValue);
    }

    private void setProfileCountryCode(Field field, Profile profile, CountryCode value) {
        CountryCode enumValue = CountryCode.fromCountryName(value.toString());
        ReflectionUtils.setField(field, profile, enumValue);
    }

    private void setProfileDate(Field field, Profile profile, String key, Object value) throws ParseException {
        // Convert String value to Date
        Date dateValue;
        SimpleDateFormat dateFormat;
        if (((String) value).matches("\\d{4}-\\d{2}-\\d{2}")) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//                        } else if (((String) value).matches("\\d{2}-\\d{2}-\\d{4}")) {
//                            dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        } else {
            throw new IllegalArgumentException("Invalid date format for field: " + key);
        }
        dateValue = dateFormat.parse((String) value);
        ReflectionUtils.setField(field, profile, dateValue);
    }


    private Socials identifyProfileSocials(Socials socials, Object value) {
        // Handle nested "socials" field
        Map<String, Object> socialsFields = (Map<String, Object>) value;
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
        return socials;
    }

    private void checkFieldsToUpdate(Map<String, Object> fields, Profile profile) {
        UpdateProfileDetails updateProfileDetails = new UpdateProfileDetails();
        Class<?> updateProfileDetailsClass = updateProfileDetails.getClass();

        for (String key : fields.keySet()) {

            try {
                // Check if the field exists in the UpdateProfileDetails class
                Field updateDetailsField = updateProfileDetailsClass.getDeclaredField(key); //здесь выбрасывается исключение
                updateDetailsField.setAccessible(true);
                updateDetailsField.get(updateProfileDetails); //Object fieldValue =

            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new ProfileUpdateException(e.getMessage());
            }
        }
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

    /**
     * for testing
     *
     * @param email
     * @return
     */
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
        Profile profile = profileRepository.findByUid(uid).orElseThrow(() -> new ProfileNotFoundException(uid));

        return profileDtoBuilder.buildProfileDetails(profile);
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

