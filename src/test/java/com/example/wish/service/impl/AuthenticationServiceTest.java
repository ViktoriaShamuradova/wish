package com.example.wish.service.impl;

import com.example.wish.component.NotificationManagerService;
import com.example.wish.dto.AuthRequest;
import com.example.wish.dto.AuthResponse;
import com.example.wish.dto.EmailVerificationRequest;
import com.example.wish.dto.RegistrationRequest;
import com.example.wish.entity.*;
import com.example.wish.exception.profile.ProfileException;
import com.example.wish.exception.profile.ProfileExistException;
import com.example.wish.model.CurrentProfile;
import com.example.wish.repository.ProfileRepository;
import com.example.wish.repository.TokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
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
    private TokenRepository tokenRepository;

    @Mock
    private EmailValidator emailValidator;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationManagerService notificationManagerService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private static final int EXPIRE_MINUTES_FOR_REGISTRATION = 60;
    private static final int EXPIRE_MINUTES_FOR_PASSWORD = 10;


    @Test
    void register_ValidRequest_SuccessfulRegistration() {

        RegistrationRequest registrationRequest = new RegistrationRequest("kateshemem@gmail.com", "Rumbling!0", "Rumbling!0");
        Profile mockProfile = createProfile(registrationRequest);
        when(profileRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(profileRepository.save(any())).thenReturn(mockProfile);
        when(passwordEncoder.encode(any())).thenReturn("dvbdjvbdjvbdjvbjdvb");
        when(jwtService.generateAccessToken(any())).thenReturn("access_token");


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
    public void authenticate_ValidCredentials_ReturnsAuthResponse() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        AuthRequest authRequest = new AuthRequest(email, password);
        Profile profile = createProfile(authRequest);
        List<Token> tokens = new ArrayList<>();
        String jwt = "access_token";
        when(profileRepository.findByEmail(email)).thenReturn(Optional.of(profile));
        when(jwtService.generateAccessToken(any())).thenReturn(jwt);
        when(tokenRepository.findAllValidTokensByProfile(profile.getId())).thenReturn(tokens);
        when(tokenRepository.save(any())).thenReturn(any());
        // Act
        AuthResponse result = authenticationService.authenticate(authRequest);

        // Assert
        assertNotNull(result);
        assertEquals("access_token", result.getToken());
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        ArgumentCaptor<CurrentProfile> captor = ArgumentCaptor
                .forClass(CurrentProfile.class); //нужен для объектов которые создаются внутри проверяемого метода
        ArgumentCaptor<Token> tokenArgumentCaptor = ArgumentCaptor
                .forClass(Token.class);
        verify(jwtService).generateAccessToken(captor.capture());
        verify(tokenRepository).save(tokenArgumentCaptor.capture());

        CurrentProfile value = captor.getValue();
        assertThat(value.getEmail()).isEqualTo(profile.getEmail());

        Token tokenValue = tokenArgumentCaptor.getValue();
        assertThat(tokenValue.getToken()).isEqualTo(jwt);
    }

    @Test
    public void authenticate_profileLocked() {
        String email = "test@example.com";
        String password = "password";
        AuthRequest authRequest = new AuthRequest(email, password);
        Profile profile = createProfile(authRequest);
        profile.setEnabled(true);
        profile.setLocked(true);

        when(profileRepository.findByEmail(email)).thenReturn(Optional.of(profile));

        doThrow(LockedException.class).when(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        assertThrows(ProfileException.class, () -> authenticationService.authenticate(authRequest));
    }


    @Test
    public void authenticate_InvalidEmailOrPassword_ThrowsProfileException() {
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
    public void authenticate_ProfileNotFound_ThrowsProfileException() {
        // Arrange
        String email = "test@example.com";
        String password = "password";
        AuthRequest authRequest = new AuthRequest("test@example.com", "password");
        when(profileRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ProfileException.class, () -> authenticationService.authenticate(authRequest));
    }

    @Test
    public void authenticate_ProfileProviderGoogle_ThrowsProfileException() {
        // Arrange
        AuthRequest authRequest = new AuthRequest("test@example.com", "password");
        Profile mockProfile = createProfile(authRequest);
        mockProfile.setProvider(Provider.GOOGLE);
        when(profileRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(mockProfile));

        // Act & Assert
        assertThrows(ProfileException.class, () -> authenticationService.authenticate(authRequest));
    }

    @Test
    public void verifyEmailWithEmailExistsAndIsRegistrationTrueThrowException() {
        EmailVerificationRequest request = new EmailVerificationRequest("test@example.com", "123456", true);

        when(profileRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new Profile()));

        // Test the exception
        assertThrows(ProfileExistException.class, () -> authenticationService.verifyEmail(request));

        // Verify that notificationManagerService methods were not called
        verify(notificationManagerService, never()).sendOnePasswordForEmailVerification(anyString(), anyString(), anyInt());
        verify(notificationManagerService, never()).sendOnePasswordForResetPassword(anyString(), anyString(), anyInt());
    }

    @Test
    public void verifyEmailWithEmailNotExistAndIsRegistrationTrue_WillSendNotification() {
        EmailVerificationRequest request = new EmailVerificationRequest("test@example.com", "123456", true);

        when(profileRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        authenticationService.verifyEmail(request);

        verify(notificationManagerService).sendOnePasswordForEmailVerification(
                request.getEmail(),
                request.getOtp(),
                EXPIRE_MINUTES_FOR_REGISTRATION
        );
        verify(notificationManagerService, never()).sendOnePasswordForResetPassword(anyString(), anyString(), anyInt());
    }

    @Test
    public void verifyEmailWithEmailNotExistAndIsRegistrationFalse_WillSendNotification() {
        EmailVerificationRequest request = new EmailVerificationRequest("test@example.com", "123456", false);

        when(profileRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new Profile()));

        authenticationService.verifyEmail(request);

        verify(notificationManagerService).sendOnePasswordForResetPassword(
                request.getEmail(),
                request.getOtp(),
                EXPIRE_MINUTES_FOR_PASSWORD
        );
        verify(notificationManagerService, never()).sendOnePasswordForEmailVerification(anyString(), anyString(), anyInt());
    }

    @Test
    public void verifyEmailWithEmailIsEmptyAndIsRegistrationFalse_TrowException() {
        EmailVerificationRequest request = new EmailVerificationRequest("test@example.com", "123456", false);

        when(profileRepository.findByEmail(request.getEmail())).thenReturn(Optional.ofNullable(null));

        // Test the exception
        assertThrows(ProfileExistException.class, () -> authenticationService.verifyEmail(request));

        // Verify that notificationManagerService methods were not called
        verify(notificationManagerService, never()).sendOnePasswordForEmailVerification(anyString(), anyString(), anyInt());
        verify(notificationManagerService, never()).sendOnePasswordForResetPassword(anyString(), anyString(), anyInt());
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
