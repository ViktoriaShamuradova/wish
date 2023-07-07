package com.example.wish.service.impl;

import com.example.wish.component.NotificationManagerService;
import com.example.wish.dto.*;
import com.example.wish.entity.*;
import com.example.wish.exception.CantCompleteClientRequestException;
import com.example.wish.exception.profile.CurrentProfileNotFoundException;
import com.example.wish.exception.profile.ProfileException;
import com.example.wish.exception.profile.ProfileExistException;
import com.example.wish.model.CurrentProfile;
import com.example.wish.repository.ProfileRepository;
import com.example.wish.util.DataBuilder;
import lombok.NonNull;
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

import javax.security.auth.message.AuthException;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    private final ProfileRepository profileRepository;
    private final EmailValidator emailValidator;
    private final JwtService jwtService;
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

    /**
     * before creating profile user confirmed his email
     *
     * @param registerRequest
     * @return
     */
    @Transactional
    public AuthResponse register(RegistrationRequest registerRequest) {
        Optional<Profile> byEmail = profileRepository.findByEmail(registerRequest.getEmail());

        if (byEmail.isPresent()) {
            throw new ProfileExistException("This email " + registerRequest.getEmail() + " already exists");
        } else {
            var profile = createProfile(registerRequest);
            profile.setEnabled(true); //true - потому что перед данным запросом юзер уже получил письмо не почту, а значит доступен
            Profile saved = profileRepository.save(profile);

            CurrentProfile currentProfile = new CurrentProfile(saved);

            var accessToken = jwtService.generateAccessToken(currentProfile);
            var refreshToken = jwtService.generateRefreshToken(currentProfile);

            return createAuthResponse(accessToken, refreshToken);
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse authenticate(@NotNull AuthRequest authRequest) {
        var profile = profileRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new ProfileException("Email not found: " + authRequest.getEmail()));

        if (profile.getProvider() == Provider.GOOGLE) {
            throw new ProfileException("User registered with Google. Please use the Google sign-in URL.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getEmail(),
                            authRequest.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new ProfileException("Invalid email or password.");
        } catch (AuthenticationException e) {
            throw new ProfileException("Please confirm your email address to sign in.");
        }

        CurrentProfile currentProfile = new CurrentProfile(profile);

        var accessToken = jwtService.generateAccessToken(currentProfile);
        var refreshToken = jwtService.generateRefreshToken(currentProfile);

        return createAuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public void verifyEmail(EmailVerificationRequest emailVerificationRequest) {
        Optional<Profile> byEmail = profileRepository.findByEmail(emailVerificationRequest.getEmail());

        if (emailVerificationRequest.isRegistration()) {
            if (byEmail.isPresent()) {
                throw new ProfileExistException("This email " + emailVerificationRequest.getEmail() + " already exists");
            }
            notificationManagerService.sendOnePasswordForEmailVerification(emailVerificationRequest.getEmail(),
                    emailVerificationRequest.getOtp(), EXPIRE_MINUTES_FOR_REGISTRATION);
        } else {
            if (byEmail.isEmpty()) {
                throw new ProfileExistException("This email " + emailVerificationRequest.getEmail() + " does not exists");
            }
            notificationManagerService.sendOnePasswordForResetPassword(emailVerificationRequest.getEmail(),
                    emailVerificationRequest.getOtp(), EXPIRE_MINUTES_FOR_PASSWORD);
        }
    }

    @Transactional
    public AuthResponse updatePassword(UpdatePasswordRequest updatePasswordRequest) {
        Profile profile = profileRepository.findByEmail(updatePasswordRequest.getEmail())
                .orElseThrow(() -> new ProfileException("email " + updatePasswordRequest.getEmail() + " not found"));

        profile.setPassword(passwordEncoder.encode(updatePasswordRequest.getPassword()));

        profileRepository.save(profile);

        CurrentProfile currentProfile = new CurrentProfile(profile);

        var accessToken = jwtService.generateAccessToken(currentProfile);
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

    public AuthResponse refreshAccessToken(@NonNull String refreshToken) {

        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        if (jwtService.isTokenValid(refreshToken, userDetails)) {
            String accessToken = jwtService.generateAccessToken(userDetails);

            return createAuthResponse(accessToken, refreshToken);
        }
        return new AuthResponse(null, null);
    }

    /**
     * обновляем рефреш токен
     *
     * @param refreshToken
     * @return
     * @throws AuthException
     */
    public AuthResponse refresh(@NonNull String refreshToken) throws AuthException {
        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        if (jwtService.isTokenValid(refreshToken, userDetails)) {
            final String accessToken = jwtService.generateAccessToken(userDetails);
            final String newRefreshToken = jwtService.generateRefreshToken(userDetails);
            return new AuthResponse(accessToken, newRefreshToken);

        }
        throw new AuthException("Not valid refresh token");
    }

    private Profile createProfile(RegistrationRequest registerRequest) {
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
                .provider(Provider.LOCAL)
                .locked(false)
                .enabled(false) //value before verification email. user need to confirm email
                .build();
    }

    private AuthResponse createAuthResponse(String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}


//  private int generateRandomNumber() {
//        Random random = new Random();
//        return random.nextInt(90000) + 10000;
//    }

//    private ConfirmationToken createConfirmationToken(Profile profile, String code, int minutes) {
//        return new ConfirmationToken(
//                code,
//                LocalDateTime.now(),
//                LocalDateTime.now().plusMinutes(minutes),
//                profile);
//    }
//
//private ConfirmationToken generateToken(Profile profile, int minutes) {
//    // String token = UUID.randomUUID().toString();
//
//    int randomNumber = generateRandomNumber(); //five digits
//    ConfirmationToken confirmationToken = createConfirmationToken(profile, Integer.toString(randomNumber), minutes);
//
//    for (int i = 0; confirmationTokenService.countByToken(confirmationToken.getToken()) > 0; i++) {
//        randomNumber = generateRandomNumber();
//        confirmationToken = createConfirmationToken(profile, Integer.toString(randomNumber), minutes);
//        if (i >= maxTryCountToGenerate) {
//            throw new CantCompleteClientRequestException("Can't generate unique token for profile: " + randomNumber + ": maxTryCountToGenerateUid detected");
//        }
//    }
//    return confirmationToken;
//}

//
//@Transactional
//public ConfirmationTokenDto registerProfileCreateAndSendEmailForVerification(RegistrationRequest registerRequest) {
//    boolean isValidEmail = emailValidator.test(registerRequest.getEmail());
//    if (!isValidEmail) throw new EmailException(registerRequest.getEmail());
//
//    Optional<Profile> byEmail = profileRepository.findByEmail(registerRequest.getEmail());
//
//    if (byEmail.isPresent()) {
//        Profile profile = byEmail.get();
//        if (!profile.getEnabled()) {
//            ConfirmationToken token = generateTokenForRegistration(profile);
//            confirmationTokenService.save(token);
//            notificationManagerService.sendConfirmationTokenForRegistration(profile, token);
//            LOGGER.info(token.getToken());
//            return new ConfirmationTokenDto(token.getToken());
//        } else {
//            throw new ProfileExistException("This email " + registerRequest.getEmail() + " already exists");
//        }
//    } else {
//        var profile = createProfile(registerRequest);
//
//        profileRepository.save(profile);
//        ConfirmationToken token = generateTokenForRegistration(profile);
//        confirmationTokenService.save(token);
//
//        LOGGER.info(token.getToken());
//
//        notificationManagerService.sendConfirmationTokenForRegistration(profile, token);
//        return new ConfirmationTokenDto(token.getToken());
//    }
//}

//  private ConfirmationToken generateTokenForPassword(Profile profile) {
//        return generateToken(profile, 10);
//    }

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


// /**
//     * use for if token for registration is not valid and user want to percieve one more/
//     * удаляем старый токен. отправляем новый
//     * найти токен по почте и удалить его. Проверить, если почты такой нет/ профиль уже есть в базе данных
//     *
//     * @param emailRequest
//     */
//    @Transactional
//    public void refreshRegistrationToken(EmailRequest emailRequest) {
//        Profile profile = profileRepository.findByEmail(emailRequest.getEmail())
//                .orElseThrow(() -> new ProfileNotFoundException(emailRequest.getEmail()));
//        confirmationTokenService.deleteToken(profile);
//
//        ConfirmationToken token = generateTokenForRegistration(profile);
//        confirmationTokenService.save(token);
//        LOGGER.info("Token " + token.getToken());
//        // notificationManagerService.sendConfirmationTokenForRegistration(profile, token);
//    }

//  @Transactional
//    public ProfileDetailsForPasswordDto checkTokenForPassword(ConfirmationTokenDto tokenRequest) {
//        ConfirmationToken confirmationToken = confirmationTokenService
//                .find(tokenRequest.getToken())
//                .orElseThrow(() -> new TokenException("token not found"));
//
//        if (confirmationToken.getConfirmedAt() != null) {
//            throw new TokenException("token already confirmed");
//        }
//
//        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
//
//        if (expiredAt.isBefore(LocalDateTime.now())) {
//            throw new TokenException("token expired");
//        }
//
//        confirmationTokenService.setConfirmedAt(tokenRequest.getToken());
//        return new ProfileDetailsForPasswordDto(confirmationToken.getProfile().getId());
//    }

// private ConfirmationToken generateTokenForRegistration(Profile profile) {
//        return generateToken(profile, EXPIRE_MINUTES_FOR_REGISTRATION);
//    }
//
//    private ConfirmationToken buildTokenForRegistration(Profile profile, String otp) {
//        return createConfirmationToken(profile, otp, EXPIRE_MINUTES_FOR_REGISTRATION);
//    }

//   @Transactional
//    public ConfirmationTokenDto createTokenForPassword(EmailRequest forgotPasswordRequest) {
//        boolean isValidEmail = emailValidator.test(forgotPasswordRequest.getEmail());
//        if (!isValidEmail) throw new EmailException(forgotPasswordRequest.getEmail());
//
//        Profile profile = profileRepository.findByEmail(forgotPasswordRequest.getEmail())
//                .orElseThrow(() -> new ProfileNotFoundException(forgotPasswordRequest.getEmail()));
//
//        ConfirmationToken confirmationToken = generateTokenForPassword(profile);
//        LOGGER.info("token " + confirmationToken.getToken());
//        confirmationTokenService.save(confirmationToken);
//
//        //notificationManagerService.sendConfirmationTokenForPassword(profile, confirmationToken, EXPIRE_MINUTES_FOR_PASSWORD);
//
//        return new ConfirmationTokenDto(confirmationToken.getToken());
//    }

// @Transactional
//    public AuthResponse confirmRegister(ConfirmationTokenDto tokenRequest) {
//        ConfirmationToken confirmationToken = confirmationTokenService
//                .find(tokenRequest.getToken())
//                .orElseThrow(() -> new TokenException("token not found"));
//
//        if (confirmationToken.getConfirmedAt() != null) {
//            throw new TokenException("email already confirmed");
//        }
//
//        LocalDateTime expiredAt = confirmationToken.getExpiresAt();
//
//        if (expiredAt.isBefore(LocalDateTime.now())) {
//            throw new TokenException("token expired");
//        }
//
//        confirmationTokenService.setConfirmedAt(tokenRequest.getToken());
//        profileRepository.enableProfile(confirmationToken.getProfile().getEmail());
//
//        CurrentProfile currentProfile = new CurrentProfile(confirmationToken.getProfile());
//
//        var accessToken = jwtService.generateToken(currentProfile);
//        var refreshToken = jwtService.generateRefreshToken(currentProfile);
//
//        return createAuthResponse(accessToken, refreshToken);
//    }
