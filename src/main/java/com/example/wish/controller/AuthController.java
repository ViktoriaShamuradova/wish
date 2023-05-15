package com.example.wish.controller;

import com.example.wish.dto.*;
import com.example.wish.service.impl.AuthenticationService;
import com.example.wish.service.impl.JwtService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.message.AuthException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping("/v1/demo/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * send message to verify email.
     * it url also use to send message again, for registration, forgot password
     * @param emailVerificationRequest
     * @return
     */
    @PostMapping("/email-verification")
    public ResponseEntity emailVerification(@RequestBody @Valid EmailVerificationRequest emailVerificationRequest) {
        authenticationService.verifyEmail(emailVerificationRequest);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/sign-up")
    @ApiOperation("Request - RegisterRequest with email, password and confirmPassword. " +
            "If the mail is not valid - a response is returned with the status BAD_REQUEST with message \"email not valid\"." +
            "If email already exist - a response is returned with status BAD_REQUEST with message \"email already exists\"")
    public ResponseEntity register(@RequestBody @Valid RegistrationRequest registerRequest) {
        authenticationService.register(registerRequest);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PostMapping("/sign-in")
    @ApiOperation("User authentication. A response is returned with an access token and a refresh token if password correct." +
            "If password or email not correct return response with status bad request")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody @Valid AuthRequest authRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(authRequest));
    }

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
