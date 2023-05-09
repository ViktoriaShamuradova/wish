package com.example.wish.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EmailVerificationRequest {

   // @Email(message = "not valid email") некорректно отображается ошибка, посмотреть этот момент. Оставляем пока проверку валидности в сервисе
    private final String email;
    //сюда можно аннотацию проверки на пятизначность
    private final String otp;
}
