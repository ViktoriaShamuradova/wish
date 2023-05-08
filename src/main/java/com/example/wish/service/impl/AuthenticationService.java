package com.example.wish.service.impl;

import com.example.wish.component.NotificationManagerService;
import com.example.wish.controller.AuthController;
import com.example.wish.controller.ProfileController;
import com.example.wish.dto.*;
import com.example.wish.entity.*;
import com.example.wish.exception.CantCompleteClientRequestException;
import com.example.wish.exception.auth.EmailException;
import com.example.wish.exception.auth.TokenException;
import com.example.wish.exception.profile.*;
import com.example.wish.model.CurrentProfile;
import com.example.wish.repository.ProfileRepository;
import com.example.wish.util.DataBuilder;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    private final ProfileRepository profileRepository;
    private final EmailValidator emailValidator;
    private final JwtService jwtService;
    private final ConfirmationTokenService confirmationTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final NotificationManagerService notificationManagerService;
    private final UserDetailsService userDetailsService;

    private static final int EXPIRE_MINUTES_FOR_REGISTRATION = 60;
    private static final int EXPIRE_MINUTES_FOR_PASSWORD = 10;

    @Value("${generate.uid.alphabet}")
    private String generateUidAlphabet;
    @Value("${generate.uid.suffix.length}")
    private int generateUidSuffixLength;
    @Value("${generate.uid.max.try.count}")
    private int maxTryCountToGenerate;


    @Transactional
    public ConfirmationTokenDto register(RegisterRequest registerRequest) {
        boolean isValidEmail = emailValidator.test(registerRequest.getEmail());
        if (!isValidEmail) throw new EmailException(registerRequest.getEmail());

        Optional<Profile> byEmail = profileRepository.findByEmail(registerRequest.getEmail());

        if (byEmail.isPresent()) {
            Profile profile = byEmail.get();
            if (!profile.getEnabled()) {
                ConfirmationToken token = generateTokenForRegistration(profile);
                confirmationTokenService.save(token);
                //  notificationManagerService.sendConfirmationTokenForRegistration(profile, token);
                LOGGER.info(token.getToken());
                return new ConfirmationTokenDto(token.getToken());
            } else {
                throw new ProfileExistException("This email " + registerRequest.getEmail() + " already exists");
            }
        } else {
            var profile = createProfile(registerRequest);

            profileRepository.save(profile);
            ConfirmationToken token = generateTokenForRegistration(profile);
            confirmationTokenService.save(token);

            LOGGER.info(token.getToken());

            // notificationManagerService.sendConfirmationTokenForRegistration(profile, token);
            return new ConfirmationTokenDto(token.getToken());
        }
    }

    /**
     * use for if token for registration is not valid and user want to percieve one more/
     * удаляем старый токен. отправляем новый
     * найти токен по почте и удалить его. Проверить, если почты такой нет/ профиль уже есть в базе данных
     *
     * @param emailRequest
     */
    @Transactional
    public void refreshRegistrationToken(EmailRequest emailRequest) {
        Profile profile = profileRepository.findByEmail(emailRequest.getEmail())
                .orElseThrow(() -> new ProfileNotFoundException(emailRequest.getEmail()));
        confirmationTokenService.deleteToken(profile);

        ConfirmationToken token = generateTokenForRegistration(profile);
        confirmationTokenService.save(token);
        LOGGER.info("Token " + token.getToken());
        // notificationManagerService.sendConfirmationTokenForRegistration(profile, token);
    }

    @Transactional
    public ConfirmationTokenDto createTokenForPassword(EmailRequest forgotPasswordRequest) {
        boolean isValidEmail = emailValidator.test(forgotPasswordRequest.getEmail());
        if (!isValidEmail) throw new EmailException(forgotPasswordRequest.getEmail());

        Profile profile = profileRepository.findByEmail(forgotPasswordRequest.getEmail())
                .orElseThrow(() -> new ProfileNotFoundException(forgotPasswordRequest.getEmail()));

        ConfirmationToken confirmationToken = generateTokenForPassword(profile);
        LOGGER.info("token " + confirmationToken.getToken());
        confirmationTokenService.save(confirmationToken);

        //notificationManagerService.sendConfirmationTokenForPassword(profile, confirmationToken, EXPIRE_MINUTES_FOR_PASSWORD);

        return new ConfirmationTokenDto(confirmationToken.getToken());
    }

    @Transactional
    public ProfileDetailsForPasswordDto checkTokenForPassword(ConfirmationTokenDto tokenRequest) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .find(tokenRequest.getToken())
                .orElseThrow(() -> new TokenException("token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new TokenException("token already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new TokenException("token expired");
        }

        confirmationTokenService.setConfirmedAt(tokenRequest.getToken());
        return new ProfileDetailsForPasswordDto(confirmationToken.getProfile().getId());
    }

    @Transactional
    public AuthResponse updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        Profile profile = profileRepository.findById(updatePasswordRequest.getProfileId())
                .orElseThrow(() -> new ProfileExistException("profile not found with id " + updatePasswordRequest.getProfileId()));

        profile.setPassword(passwordEncoder.encode(updatePasswordRequest.getPassword()));

        profileRepository.save(profile);

        CurrentProfile currentProfile = new CurrentProfile(profile);

        var accessToken = jwtService.generateToken(currentProfile);
        var refreshToken = jwtService.generateRefreshToken(currentProfile);

        return createAuthResponse(accessToken, refreshToken);

    }

    @Transactional
    public AuthResponse confirmRegister(ConfirmationTokenDto tokenRequest) {
        ConfirmationToken confirmationToken = confirmationTokenService
                .find(tokenRequest.getToken())
                .orElseThrow(() -> new TokenException("token not found"));

        if (confirmationToken.getConfirmedAt() != null) {
            throw new TokenException("email already confirmed");
        }

        LocalDateTime expiredAt = confirmationToken.getExpiresAt();

        if (expiredAt.isBefore(LocalDateTime.now())) {
            throw new TokenException("token expired");
        }

        confirmationTokenService.setConfirmedAt(tokenRequest.getToken());
        profileRepository.enableProfile(confirmationToken.getProfile().getEmail());

        CurrentProfile currentProfile = new CurrentProfile(confirmationToken.getProfile());

        var accessToken = jwtService.generateToken(currentProfile);
        var refreshToken = jwtService.generateRefreshToken(currentProfile);

        return createAuthResponse(accessToken, refreshToken);
    }


    @Transactional(readOnly = true)
    public AuthResponse authenticate(AuthRequest authRequest) {
        //exception will be throwing if username or password not correct
        //or user not confirm his email
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new ProfileException("wrong password or email");
        } catch (AuthenticationException e) {
            throw new ProfileException("Confirm your email address to log in.");
        }

//if we here, that means user is authenticated
        var profile = profileRepository.findByEmail(authRequest.getEmail())
                .orElseThrow();

        CurrentProfile currentProfile = new CurrentProfile(profile);

        var accessToken = jwtService.generateToken(currentProfile);
        var refreshToken = jwtService.generateRefreshToken(currentProfile);

        return createAuthResponse(accessToken, refreshToken);
    }

    private String generateProfileUid(String email) {
        String baseUid = DataBuilder.buildProfileUid(email);

        for (int i = 0; profileRepository.countByUid(baseUid) > 0; i++) {
            baseUid = DataBuilder.rebuildUidWithRandomSuffix(baseUid, generateUidAlphabet, generateUidSuffixLength);
            if (i >= maxTryCountToGenerate) {
                throw new CantCompleteClientRequestException("Can't generate unique uid for profile: " + baseUid + ": maxTryCountToGenerateUid detected");
            }
        }
        return baseUid;
    }

    public CurrentProfile getCurrentProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new CurrentProfileNotFoundException("No authentication found");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CurrentProfile) {
            return ((CurrentProfile) principal);
        } else {
            throw new CurrentProfileNotFoundException("No current profile found");
        }
    }

    private ConfirmationToken generateTokenForRegistration(Profile profile) {
        return generateToken(profile, EXPIRE_MINUTES_FOR_REGISTRATION);
    }

    private ConfirmationToken generateTokenForPassword(Profile profile) {
        return generateToken(profile, 10);
    }

    private ConfirmationToken generateToken(Profile profile, int minutes) {
        // String token = UUID.randomUUID().toString();
        Random random = new Random();
        int randomNumber = random.nextInt(90000) + 10000; //five digits
        ConfirmationToken confirmationToken = new ConfirmationToken(
                Integer.toString(randomNumber),
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(minutes),
                profile);

        for (int i = 0; confirmationTokenService.countByToken(confirmationToken.getToken()) > 0; i++) {
            randomNumber = random.nextInt(90000) + 10000;
            confirmationToken = new ConfirmationToken(
                    Integer.toString(randomNumber),
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(minutes),
                    profile);
            if (i >= maxTryCountToGenerate) {
                throw new CantCompleteClientRequestException("Can't generate unique token for profile: " + randomNumber + ": maxTryCountToGenerateUid detected");
            }
        }

        return confirmationToken;
    }

    public AuthResponse refreshAccessToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        final String refreshToken;
        final String username;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new TokenException("refresh token is missing");
        } else {
            refreshToken = authHeader.substring(7);
            try {
                username = jwtService.extractUsername(refreshToken);

            } catch (Exception e) {
                throw new TokenException(e.getMessage());
            }

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (jwtService.isTokenValid(refreshToken, userDetails)) {
                String accessToken = jwtService.generateToken(userDetails);

                return createAuthResponse(accessToken, refreshToken);
            }
        }
        return null;
    }

    private Profile createProfile(RegisterRequest registerRequest) {
        return Profile.builder()
                .active(true)
                .uid(generateProfileUid(registerRequest.getEmail()))
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(Role.USER)
                .status(ProfileStatus.RED)
                .statusLevel(ProfileStatusLevel.PERSON)
                .created(new Timestamp(System.currentTimeMillis()))
                .karma(0)
                .locked(false) //value before verification email. user need to confirm email
                .enabled(false)
                .build();
    }

    private AuthResponse createAuthResponse(String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }




    // private String generateProfileUid(String firstname, String lastname) {
    //        String baseUid = DataBuilder.buildProfileUid(firstname, lastname);//firstname-lastname
    //        //если не сущ, то возвращаем только что сгенерированный uid
    //        //если существует, то к baseuid добавляем рандомную строку
    //
    //        for (int i = 0; profileRepository.countByUid(baseUid) > 0; i++) {
    //            baseUid = DataBuilder.rebuildUidWithRandomSuffix(baseUid, generateUidAlphabet, generateUidSuffixLength);
    //            if (i >= maxTryCountToGenerateUid) { //выбрасываем исключение, когда слишком много генерировался uid
    //                throw new CantCompleteClientRequestException("Can't generate unique uid for profile: " + baseUid + ": maxTryCountToGenerateUid detected");
    //            }
    //        }
    //        return baseUid;
    //    }
}
