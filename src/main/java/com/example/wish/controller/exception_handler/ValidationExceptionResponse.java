package com.example.wish.controller.exception_handler;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;
import java.util.Map;

@Data
@RequiredArgsConstructor
public class ValidationExceptionResponse {

    private final HttpStatus httpStatus;
    private final ZonedDateTime timestamp;
    private final Map<String, String> messages;
}
