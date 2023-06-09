package com.example.wish.service.impl;

import com.example.wish.dto.AuthRequest;
import com.example.wish.dto.AuthResponse;
import com.example.wish.dto.RegistrationRequest;
import com.example.wish.entity.*;
import com.example.wish.exception.profile.ProfileException;
import com.example.wish.exception.profile.ProfileExistException;
import com.example.wish.model.CurrentProfile;
import com.example.wish.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private EmailValidator emailValidator;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;


    @Test
    void register_ValidRequest_SuccessfulRegistration() {

        RegistrationRequest registrationRequest = new RegistrationRequest("kateshemem@gmail.com", "Rumbling!0", "Rumbling!0");
        Profile mockProfile = createProfile(registrationRequest);
        when(profileRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(profileRepository.save(any())).thenReturn(mockProfile);
        when(passwordEncoder.encode(any())).thenReturn("dvbdjvbdjvbdjvbjdvb");
        when(jwtService.generateAccessToken(any())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh_token");


        AuthResponse result = authenticationService.register(registrationRequest);

        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode(registrationRequest.getPassword());//роверяет что был енкодер вызван с этим паролем, один раз вызвался метод

        ArgumentCaptor<CurrentProfile> captor = ArgumentCaptor.forClass(CurrentProfile.class); //нужен для объектов которые создаются внутри проверяемого метода
        verify(jwtService).generateAccessToken(captor.capture());
        CurrentProfile value = captor.getValue();
        assertThat(value.getEmail()).isEqualTo(mockProfile.getEmail());

    }

    @Test
    void register_ProfileExists_ThrowsProfileExistException() {

        RegistrationRequest registrationRequest = new RegistrationRequest("existing_email@gmail.com", "Rumbling!0", "Rumbling!0");
        Profile profile = createProfile(registrationRequest);
        when(profileRepository.findByEmail(registrationRequest.getEmail()))
                .thenReturn(Optional.of(profile));

        assertThrows(ProfileExistException.class, () -> authenticationService.register(registrationRequest));
    }

    @Test
    public void testAuthenticate_ValidCredentials_ReturnsAuthResponse() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        AuthRequest authRequest = new AuthRequest(email, password);
        Profile profile = createProfile(authRequest);
        when(profileRepository.findByEmail(email)).thenReturn(Optional.of(profile));
        when(jwtService.generateAccessToken(any())).thenReturn("access_token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh_token");

        // Act
        AuthResponse result = authenticationService.authenticate(authRequest);

        // Assert
        assertNotNull(result);
        assertEquals("access_token", result.getAccessToken());
        assertEquals("refresh_token", result.getRefreshToken());
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        ArgumentCaptor<CurrentProfile> captor = ArgumentCaptor.forClass(CurrentProfile.class); //нужен для объектов которые создаются внутри проверяемого метода
        verify(jwtService).generateAccessToken(captor.capture());
        CurrentProfile value = captor.getValue();
        assertThat(value.getEmail()).isEqualTo(profile.getEmail());
    }

    @Test
    public void testAuthenticate_InvalidEmailOrPassword_ThrowsProfileException() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("test@example.com", "incorrect_password");
        Profile profile = createProfile(authRequest);

        when(profileRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(profile));
        doThrow(BadCredentialsException.class).when(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        // Act & Assert
        assertThrows(ProfileException.class, () -> authenticationService.authenticate(authRequest));
    }

    @Test
    public void testAuthenticate_ProfileNotFound_ThrowsProfileException() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        AuthRequest authRequest = new AuthRequest("test@example.com", "password");
        when(profileRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProfileException.class, () -> authenticationService.authenticate(authRequest));
    }

    @Test
    public void testAuthenticate_ProfileProviderGoogle_ThrowsProfileException() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("test@example.com", "password");
        Profile mockProfile = createProfile(authRequest);
        mockProfile.setProvider(Provider.GOOGLE);
        when(profileRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(mockProfile));

        // Act & Assert
        assertThrows(ProfileException.class, () -> authenticationService.authenticate(authRequest));
    }

    private Profile createProfile(AuthRequest authRequest) {
        return Profile.builder()
                .id(1L)
                .active(true)
                .uid(authRequest.getEmail())
                .email(authRequest.getEmail())
                .password(authRequest.getPassword())
                .role(Role.USER)
                .status(ProfileStatus.RED)
                .statusLevel(ProfileStatusLevel.PERSON)
                .created(new Timestamp(System.currentTimeMillis()))
                .karma(0)
                .provider(Provider.LOCAL)
                .locked(false)
                .enabled(false)
                .build();
    }

    private Profile createProfile(RegistrationRequest registerRequest) {
        return Profile.builder()
                .id(1L)
                .active(true)
                .uid(registerRequest.getEmail())
                .email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .role(Role.USER)
                .status(ProfileStatus.RED)
                .statusLevel(ProfileStatusLevel.PERSON)
                .created(new Timestamp(System.currentTimeMillis()))
                .karma(0)
                .provider(Provider.LOCAL)
                .locked(false)
                .enabled(false)
                .build();
    }


}
