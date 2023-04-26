package com.example.wish.controller.exception_handler;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Data
@RequiredArgsConstructor
public class ApiExceptionResponse {

    private final String message;
    private final HttpStatus httpStatus;
    private final ZonedDateTime timestamp;
}
