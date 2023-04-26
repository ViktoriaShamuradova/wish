package com.example.wish.controller;

import com.example.wish.dto.*;
import com.example.wish.service.impl.AuthenticationService;
import com.example.wish.service.impl.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/boomerang/v1/demo/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    //на фронте можно подсвечивать красным, если токен неверный
    @PostMapping("/sign-up")
    public ResponseEntity<ConfirmationTokenDto> register(@RequestBody @Valid RegisterRequest registerRequest) {
        return ResponseEntity.ok(authenticationService.register(registerRequest));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody @Valid AuthRequest authRequest) {
        return ResponseEntity.ok(authenticationService.authenticate(authRequest));
    }

    @PostMapping("/sign-up/confirm")
    public ResponseEntity<AuthResponse> confirmRegister(@RequestBody ConfirmationTokenDto confirmationToken) {
        return ResponseEntity.ok(authenticationService.confirmRegister(confirmationToken));
    }

    //сюда должен придти рефреш токен, чтобы измнить аксесс токен
    //рефреш токен и так проверяется в фильтре -  если он не валидный в фильтре, значит нет его и в бд
    //если рефреш токен не валидный, то зачем идти дальше в бд?
    //если он валидный, то фильтр и так проверит
    //разве что на всякий случай соранять в бд. вдруг логика приложения (фильтр) поменяется, а рефреш токен так и будет сохранен
    //если не вернулся ответ, отправить на страницу входа
    @PostMapping("/token/refresh")
    public ResponseEntity<AuthResponse> refreshToken(HttpServletRequest request,
                                                     HttpServletResponse response) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @PostMapping("/forgot_password")
    public ResponseEntity<ConfirmationTokenDto> forgotPassword(@RequestBody @Valid ForgotPasswordRequest forgotPasswordRequest) {
        return ResponseEntity.ok(authenticationService.createTokenForPassword(forgotPasswordRequest));
    }

    /**
     * выбрасывает исключение, если токен не валидный с соответствующим статусом,
     * если все ок с токеном, то возращает id profile для передачи его по следующему реквесту обновления пароля
     *
     * @param confirmationToken
     * @return
     */
    @PostMapping("/forgot_password/confirm")
    public ResponseEntity<ProfileDetailsForPasswordDto> forgotPasswordCheckToken(@RequestBody @Valid ConfirmationTokenDto confirmationToken) {
        return ResponseEntity.ok(authenticationService.checkTokenForPassword(confirmationToken));
    }

    @PostMapping("/forgot_password/update")
    public ResponseEntity<AuthResponse> updatePassword(@RequestBody @Valid UpdatePasswordRequest updatePasswordRequest) {
        return ResponseEntity.ok(authenticationService.updatePassword(updatePasswordRequest));
    }

}
