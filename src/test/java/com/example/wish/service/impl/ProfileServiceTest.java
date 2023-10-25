package com.example.wish.service.impl;


import com.example.wish.component.ProfileDtoBuilder;
import com.example.wish.dto.profile.ProfileDto;
import com.example.wish.dto.wish.AbstractWishDto;
import com.example.wish.dto.wish.WishDto;
import com.example.wish.entity.Profile;
import com.example.wish.entity.ProfileImage;
import com.example.wish.entity.WishStatus;
import com.example.wish.exception.profile.ProfileException;
import com.example.wish.exception.profile.ProfileNotFoundException;
import com.example.wish.exception.profile.ProfileUpdateException;
import com.example.wish.model.CurrentProfile;
import com.example.wish.repository.ProfileRepository;
import com.example.wish.service.ProfileImageService;
import com.example.wish.service.WishService;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private WishService wishService;

    @Mock
    private ProfileImageService profileImageService;
    @Mock
    private ProfileDtoBuilder profileDtoBuilder;
    @InjectMocks
    private ProfileServiceImpl profileService;
    private static final long OWN_PROFILE_ID = 2L;
    private static final long WISH_ID = 1L;

    @Test
    public void testUpdateByFields() throws ParseException {
        Map<String, Object> fields = new HashMap<>();
        fields.put("firstName", "John");
        fields.put("lastName", "Doe");

        Profile ownProfile = createProfile(OWN_PROFILE_ID);
        CurrentProfile currentProfile = new CurrentProfile(ownProfile);
        ProfileDto profileDto = new ProfileDto(ownProfile);
        profileDto.setFirstName((String) fields.get("firstName"));
        profileDto.setLastName((String) fields.get("lastName"));

        when(authenticationService.getCurrentProfile()).thenReturn(currentProfile);
        when(profileRepository.findById(currentProfile.getId())).thenReturn(Optional.of(ownProfile));
        when(profileDtoBuilder.buildProfileDto(any())).thenReturn(profileDto);

        profileService.updateByFields(fields);

        ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);

        verify(profileRepository).save(captor.capture());
        Profile updatedProfile = captor.getValue();
        assertEquals("John", updatedProfile.getFirstName());
        assertEquals("Doe", updatedProfile.getLastName());

    }

    @Test
    public void testUpdateByFieldsInvalidDateFormat() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("birthday", "invalid-date");

        Profile ownProfile = createProfile(OWN_PROFILE_ID);
        CurrentProfile currentProfile = new CurrentProfile(ownProfile);

        when(authenticationService.getCurrentProfile()).thenReturn(currentProfile);
        when(profileRepository.findById(currentProfile.getId())).thenReturn(Optional.of(ownProfile));

        assertThrows(IllegalArgumentException.class, () -> profileService.updateByFields(fields));

        verify(profileRepository, never()).save(any());
    }
    @Test
    public void testUpdateByFieldsFieldNotFoundInUpdateProfileDetails() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("nonExistingField", "value");

        Profile ownProfile = createProfile(OWN_PROFILE_ID);
        CurrentProfile currentProfile = new CurrentProfile(ownProfile);

        when(authenticationService.getCurrentProfile()).thenReturn(currentProfile);
        when(profileRepository.findById(currentProfile.getId())).thenReturn(Optional.of(ownProfile));

        assertThrows(ProfileUpdateException.class, () -> profileService.updateByFields(fields));

        verify(profileRepository, never()).save(any());
    }

    @Test
    public void testUploadImage() throws Exception {
        ReflectionTestUtils.setField(profileService, "maxSize", "10MB"); //чтобы не вызывать рефлексию каждый раз во всех тестовых методах

        MultipartFile mockFile = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[1024]  // an image with 1KB size
        );

        Profile ownProfile = createProfile(OWN_PROFILE_ID);
        CurrentProfile currentProfile = new CurrentProfile(ownProfile);


        when(authenticationService.getCurrentProfile()).thenReturn(currentProfile);
        when(profileRepository.findById(OWN_PROFILE_ID)).thenReturn(Optional.of(ownProfile));
        when(profileImageService.save(mockFile)).thenReturn(new ProfileImage());

        profileService.uploadImage(mockFile);

        assertThat(ownProfile.getImage()).isNotNull();
    }

    @Test
    public void testUploadImageWithInvalidFormat() {
        ReflectionTestUtils.setField(profileService, "maxSize", "10MB");

        MultipartFile mockFile = new MockMultipartFile(
                "image",
                "test.gif",
                MediaType.IMAGE_GIF_VALUE,
                new byte[1024]  // Simulate an image with 1KB size
        );

        assertThrows(HttpMediaTypeNotSupportedException.class, () -> profileService.uploadImage(mockFile));
    }

    @Test
    public void testUploadImageWithFileSizeExceedsLimit() {
        ReflectionTestUtils.setField(profileService, "maxSize", "10MB");
        MultipartFile mockFile = new MockMultipartFile(
                "image",
                "large.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                new byte[11 * 1024 * 1024]  // Simulate an image with 11MB size (exceeds the 10MB limit)
        );

        assertThrows(FileSizeLimitExceededException.class, () -> profileService.uploadImage(mockFile));
    }

    @Test
    public void testFindImageSuccess() {
        // Arrange
        String uid = "testUid";
        Profile profile = new Profile();
        ProfileImage image = new ProfileImage();
        image.setImageData(new byte[]{1, 2, 3}); // Mock image data
        profile.setImage(image);

        when(profileRepository.findByUid(uid)).thenReturn(Optional.of(profile));

        // Act
        byte[] result = profileService.findImage(uid);

        // Assert
        assertNotNull(result);
        assertArrayEquals(image.getImageData(), result);
    }

    @Test
    public void testFindImageNull() {
        String uid = "testUid";
        Profile profile = new Profile();
        profile.setUid(uid);

        when(profileRepository.findByUid(uid)).thenReturn(Optional.of(profile));

        byte[] result = profileService.findImage(uid);

        // Assert
        assertNull(result);
    }

    @Test
    public void testFindImageProfileNotFoundException() {
        String uid = "testUid";

        assertThrows(ProfileNotFoundException.class, () -> profileService.findImage(uid));
    }

    @Test
    public void testFindImageNoImageData() {
        String uid = "testUid";
        Profile profile = new Profile();

        when(profileRepository.findByUid(uid)).thenReturn(Optional.of(profile));

        byte[] result = profileService.findImage(uid);

        assertNull(result);
    }


    @Test
    public void testDeleteProfileSuccess() {
        Profile ownProfile = createProfile(OWN_PROFILE_ID);
        CurrentProfile currentProfile = new CurrentProfile(ownProfile);

        when(authenticationService.getCurrentProfile()).thenReturn(currentProfile);
        when(profileRepository.findById(OWN_PROFILE_ID)).thenReturn(Optional.of(ownProfile));
        when(wishService.getOwnWish(WishStatus.IN_PROGRESS)).thenReturn(Collections.emptyList());

        profileService.delete();

        verify(wishService, times(1)).deleteExecutingWishes(any());
        verify(profileRepository, times(1)).delete(any());
    }

    @Test()
    public void testDeleteProfileWithInProgressWishes() {
        Profile ownProfile = createProfile(OWN_PROFILE_ID);
        CurrentProfile currentProfile = new CurrentProfile(ownProfile);
        AbstractWishDto wish = createWish(WishStatus.IN_PROGRESS);
        // Arrange
        when(authenticationService.getCurrentProfile()).thenReturn(currentProfile);
        when(profileRepository.findById(any())).thenReturn(Optional.of(ownProfile));
        when(wishService.getOwnWish(WishStatus.IN_PROGRESS)).thenReturn(List.of(wish));


        ProfileException exception = assertThrows(ProfileException.class, () -> profileService.delete());
        assertEquals("can't delete profile. Profile have wishes in_progress", exception.getMessage());

        verify(wishService, never()).deleteExecutingWishes(any());
    }

    private AbstractWishDto createWish(WishStatus status) {
        WishDto wish = new WishDto();
        wish.setId(WISH_ID);
        wish.setWishStatus(status);

        return wish;
    }

    private Profile createProfile(long profileId) {
        Profile profile = new Profile();
        profile.setId(profileId);
        profile.setEmail("user@email.com");
        profile.setPassword("password");
        profile.setEnabled(true);
        profile.setLocked(false);

        return profile;
    }
}
