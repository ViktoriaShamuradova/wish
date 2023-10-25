package com.example.wish.controller;

import com.example.wish.controller.exception_handler.ValidationExceptionResponse;
import com.example.wish.dto.*;
import com.example.wish.entity.Profile;
import com.example.wish.exception.profile.ProfileException;
import com.example.wish.exception.profile.ProfileExistException;
import com.example.wish.model.CurrentProfile;
import com.example.wish.service.SocialService;
import com.example.wish.service.impl.AuthenticationService;
import com.example.wish.service.impl.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/v1/demo/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final SocialService<GoogleIdToken> googleSocialService;


    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successful registration",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid email, email already exists.",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProfileExistException.class))})})
    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegistrationRequest registerRequest) {
        AuthResponse authResponse = authenticationService.register(registerRequest);
        return new ResponseEntity<>(authResponse, HttpStatus.CREATED);
    }

    //возвращать тот же токен idTokenString
    //пользователь может войти с помощбю гугл, если он уже зареган(проверяется по почте), то возвращается тот же юзер
    //если нет в базе, то создается новый.
    //гул токен помещается в секьюрити контекст
    @PostMapping("/sign-in/google")
    public ResponseEntity<AuthResponse> loginFromGoogle(@RequestHeader("Authorization") String idTokenString) throws GeneralSecurityException, IOException {
        GoogleIdToken idToken = googleIdTokenVerifier.verify(idTokenString.substring(7));

        if (idToken != null) {
            Profile profile = googleSocialService.loginViaSocialNetwork(idToken);

            CurrentProfile currentProfile = new CurrentProfile(profile, "N/A"); //or another password

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    currentProfile, currentProfile.getPassword(), currentProfile.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            var jwt = jwtService.generateAccessToken(currentProfile);
            AuthResponse resp = AuthResponse.builder()
                    .token(jwt)
                    .build();

            // return new ResponseEntity<>(HttpStatus.CREATED);
            return ResponseEntity.ok(resp);

        } else { //значит, что токен не валидный и нужно получить id-token еще раз
            throw new ProfileException("failed register from google, invalid token");
        }
    }

    @Operation(summary = "User Authentication", description = "Authenticates a user and returns an access token and a refresh token if the password is correct.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Invalid email or password. " +
                    "Or profile registered by google provider." +
                    "Or profile locked = true, enabled = false",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ValidationExceptionResponse.class))})})
    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody @Valid AuthRequest authRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(authRequest));
    }

    @Operation(summary = "Email Verification",
            description = "Used when registering, updating password, and resubmitting one-time-password again.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update password. Email sent"),
            @ApiResponse(responseCode = "400", description = "Update password. If there is no user with this mail",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ValidationExceptionResponse.class))}),
            @ApiResponse(responseCode = "200", description = "Registering. Email sent"),
            @ApiResponse(responseCode = "400", description = "Registering. If a user with such email already exists",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ValidationExceptionResponse.class))}),
            @ApiResponse(responseCode = "400", description = "Not valid email",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ValidationExceptionResponse.class))}),
            @ApiResponse(responseCode = "500", description = "Error sending email",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ValidationExceptionResponse.class))})
    })
    @PostMapping("/email-verification")
    public ResponseEntity emailVerification(
            @RequestBody @Valid EmailVerificationRequest emailVerificationRequest) {
        authenticationService.verifyEmail(emailVerificationRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @PostMapping("/forgot-password")
    @ApiOperation("return access and refresh tokens")
    public ResponseEntity<AuthResponse> updatePassword(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest) {
        return ResponseEntity.ok(authenticationService.updatePassword(updatePasswordRequest));
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


//    //хранить в бд рефреш токен или нет
//    @PostMapping("/access-token/refresh")
////    @ApiOperation("If access token not valid need to update access token " +
////            "to give a user access to his resource without authentication. " +
////            "If refresh token not valid - return response with UNAUTHORIZED status. And user need to sign in")
//
//    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshJwtRequest request) {
//        return ResponseEntity.ok(authenticationService.refreshAccessToken(request.getRefreshToken()));
//    }

//
//    /**
//     * обновляем рефреш токен
//     *
//     * @param request
//     * @return
//     */
//    @PostMapping("/refresh-token/refresh")
//    public ResponseEntity<AuthResponse> getNewRefreshToken(@RequestBody RefreshJwtRequest request) throws AuthException {
//        final AuthResponse token = authenticationService.refresh(request.getRefreshToken());
//        return ResponseEntity.ok(token);
//    }
