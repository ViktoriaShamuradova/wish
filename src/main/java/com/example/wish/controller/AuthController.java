package com.example.wish.controller;

import com.example.wish.dto.*;
import com.example.wish.entity.Profile;
import com.example.wish.exception.profile.ProfileException;
import com.example.wish.model.CurrentProfile;
import com.example.wish.service.ProfileService;
import com.example.wish.service.SocialService;
import com.example.wish.service.impl.AuthenticationService;
import com.example.wish.service.impl.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.message.AuthException;
import javax.validation.Valid;
import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/v1/demo/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final ProfileService profileService;
    private final JwtService jwtService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final SocialService<GoogleIdToken> googleSocialService;

    @PostMapping("/sign-up")
    @ApiOperation("Request - RegisterRequest with email, password and confirmPassword. " +
            "If the mail is not valid - a response is returned with the status BAD_REQUEST with message \"email not valid\"." +
            "If email already exist - a response is returned with status BAD_REQUEST with message \"email already exists\"." +
            "Response is returned with 201 status and body with access and refresh tokens")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegistrationRequest registerRequest) {
        AuthResponse authResponse = authenticationService.register(registerRequest);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    @ApiOperation("User authentication. A response is returned with an access token and a refresh token if password correct." +
            "If email not found in database - response is return bad request with message \"email not found\"" +
            "If password not correct return response with status bad request and message \"Invalid email or password.\"" +
            "If user registered by google - response with status bad request and message \"User registered with Google. Please use the Google sign-in URL.\"" +
            "Response is returned with 200 status and body with access and refresh tokens")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody @Valid AuthRequest authRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(authRequest));
    }

    /**
     * send message to verify email.
     * it url also use to send message again, for registration, forgot password
     *
     * @param emailVerificationRequest
     * @return
     */
    @PostMapping("/email-verification")
    public ResponseEntity emailVerification(@RequestBody @Valid EmailVerificationRequest emailVerificationRequest) {
        authenticationService.verifyEmail(emailVerificationRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity removeFavoriteProfile(
            @PathVariable String email) {
        boolean isDeleted = profileService.removeProfile(email);
        return new ResponseEntity<>(isDeleted, HttpStatus.I_AM_A_TEAPOT);

    }
    //возвращать тот же токен idTokenString
    //пользователь может войти с помощбю гугла, если он уже зареган(проверяется по почте), то возвращается тот же юзер
    //если нет в базе, то создается новый.
    //а что если поместить ггул токен в секьюрити контекст?
    @PostMapping("/sign-in/google")
    public ResponseEntity<AuthResponse> loginFromGoogle(@RequestHeader("Authorization") String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = googleIdTokenVerifier.verify(idTokenString.substring(7));

        if (idToken != null) {
            Profile profile = googleSocialService.loginViaSocialNetwork(idToken);

            CurrentProfile currentProfile = new CurrentProfile(profile, "N/A"); //or another password

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    currentProfile, currentProfile.getPassword(), currentProfile.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            var accessToken = jwtService.generateAccessToken(currentProfile);
            var refreshToken = jwtService.generateRefreshToken(currentProfile);
            AuthResponse resp = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken).build();

            // return new ResponseEntity<>(HttpStatus.CREATED);
            return ResponseEntity.ok(resp);

        } else { //значит, что токен не валидный и нужно получить id-token еще раз, если хотят регаться через гугл
            throw new ProfileException("failed register from google");
        }

    }


//    private Mono<Void> successfulAuthentication(ServerWebExchange exchange, OAuth2AuthenticationToken authentication) {
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        String token = tokenManager.generate(authentication);
//        tokenManager.put(token, authentication);
//
//        exchange.getResponse().getHeaders().set(contentTypeHeader, MediaType.APPLICATION_JSON_VALUE);
//        exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(jacksonObjectMapper().writeValueAsBytes(new TokenResponse(token)))));
//        return Mono.empty();
//    }

//    private Mono<Void> unsuccessfulAuthentication(ServerWebExchange exchange, AuthenticationException exception) {
//        SecurityContextHolder.clearContext();
//        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
//        return exchange.getResponse().setComplete();
//    }

//    private static class TokenResponse {
//        private final String token;
//
//        public TokenResponse(String token) {
//            this.token = token;
//        }
//
//        public String getToken() {
//            return token;
//        }
//    }


    //хранить в бд рефреш токен или нет
    @PostMapping("/access-token/refresh")
    @ApiOperation("If access token not valid need to update access token " +
            "to give a user access to his resource without authentication. " +
            "If refresh token not valid - return response with UNAUTHORIZED status. And user need to sign in")

    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshJwtRequest request) {
        return ResponseEntity.ok(authenticationService.refreshAccessToken(request.getRefreshToken()));
    }

    /**
     * обновляем рефреш токен
     *
     * @param request
     * @return
     * @throws AuthException
     */
    @PostMapping("/refresh-token/refresh")
    public ResponseEntity<AuthResponse> getNewRefreshToken(@RequestBody RefreshJwtRequest request) throws AuthException {
        final AuthResponse token = authenticationService.refresh(request.getRefreshToken());
        return ResponseEntity.ok(token);
    }


    @PostMapping("/forgot-password")
    @ApiOperation("return access and refresh tokens")
    public ResponseEntity<AuthResponse> updatePassword(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest) {
        return ResponseEntity.ok(authenticationService.updatePassword(updatePasswordRequest));
    }

    private AuthResponse createAuthResponse(String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

}


//    @PostMapping("/sign-up")
//    @ApiOperation("Request - RegisterRequest with email, password and confirmPassword. " +
//            "The user receives a token by mail. Valid token for an hour." +
//            "If the mail is not valid, an exception ProfileNotValidException is thrown" +
//            "and a response is returned with the status BAD_REQUEST with message \"email not valid\"." +
//            "If email already exist and user enabled, an exception ProfileExistException is throw" +
//            "and a response is returned with status BAD_REQUEST with message \"email already exists\"" +
//            "if service can't generate unique token for profile or send message with token by mail: " +
//            "CantCompleteClientRequestException is throw and " +
//            "response is returned with status INTERNAL_SERVER_ERROR and message \"cant complete client request\"")
//
//
//    @PostMapping("/sign-up/confirm")
//    @ApiOperation("User registration. request - token, to confirm user's email. Return a response with status OK " +
//            "and with an access token and a refresh token." +
//            "If the token is not valid (which means we cannot find it in the database), " +
//            "a response is received with the UNAUTHORIZED status and the message \"token not found.\"" +
//            "If the token is already confirmed," +
//            "a response is received with the status UNAUTHORIZED and the message \"email already confirmed\"" +
//            "If the token has expired, a response is returned with a status UNAUTHORIZED with message \"token expired\"." +
//            "If the token is valid, then a response with tokens is returned.")
//    public ResponseEntity<AuthResponse> confirmRegister(@RequestBody ConfirmationTokenDto confirmationToken) {
//        return ResponseEntity.ok(authenticationService.confirmRegister(confirmationToken));
//    }


//
//    @PostMapping("/sign-up/refreshToken")
//    public ResponseEntity refreshToken(@RequestBody @Valid EmailRequest emailRequest) {
//        authenticationService.refreshRegistrationToken(emailRequest);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }


//    @PostMapping("/forgot_password")
//    @ApiOperation("User received token by email to confirm his email")
//    public ResponseEntity forgotPassword(@RequestBody @Valid EmailRequest forgotPasswordRequest) {
//        authenticationService.createTokenForPassword(forgotPasswordRequest);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }

//    /**
//     * выбрасывает исключение, если токен не валидный с соответствующим статусом,
//     * если все ок с токеном, то возращает id profile для передачи его по следующему реквесту обновления пароля
//     *
//     * @param confirmationToken
//     * @return
//     */
//    @PostMapping("/forgot_password/confirm")
//    @ApiOperation("return profile id for frontend if token is valid. " +
//            "if token not valid - response with UNAUTHORIZED status")
//    public ResponseEntity<ProfileDetailsForPasswordDto> forgotPasswordCheckToken(@RequestBody @Valid ConfirmationTokenDto confirmationToken) {
//        return ResponseEntity.ok(authenticationService.checkTokenForPassword(confirmationToken));
//    }
