package com.example.wish.controller.exception_handler;

import com.example.wish.exception.*;
import com.example.wish.exception.auth.EmailException;
import com.example.wish.exception.auth.TokenException;
import com.example.wish.exception.profile.CurrentProfileNotFoundException;
import com.example.wish.exception.profile.ProfileException;
import com.example.wish.exception.profile.ProfileNotFoundException;
import com.example.wish.exception.wish.WishException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {WishException.class})
    public ResponseEntity<Object> handleApiRequestException(WishException exception) {
        ApiExceptionResponse response = new ApiExceptionResponse(exception.getMessage(),
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(
                response, HttpStatus.BAD_REQUEST);
    }


    //это исключение выбрасыватся может для энамов, если значения указаны не верно, подкорректировать message
    @ExceptionHandler(value = {InvalidFormatException.class})
    public ResponseEntity<Object> handleApiRequestException(InvalidFormatException exception) {
        ApiExceptionResponse response = new ApiExceptionResponse("invalid format value",
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z"))
        );

        return new ResponseEntity<>(
                response, HttpStatus.BAD_REQUEST);
    }

    /**
     * выбрасывается если не сгенерировался uid или токен, если отсувствует сообщение при отправке сообщения на почту
     *
     * @param exception
     * @return
     */
    @ExceptionHandler(value = {CantCompleteClientRequestException.class})
    public ResponseEntity<Object> handleApiRequestException(CantCompleteClientRequestException exception) {
        ApiExceptionResponse response = new ApiExceptionResponse("cant complete client request",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(
                response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {TokenException.class})
    public ResponseEntity<Object> handleApiRequestException(TokenException exception) {
        ApiExceptionResponse response = new ApiExceptionResponse(exception.getMessage(),
                HttpStatus.UNAUTHORIZED,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(
                response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {CurrentProfileNotFoundException.class})
    public ResponseEntity<Object> handleApiRequestException(CurrentProfileNotFoundException exception) {
        ApiExceptionResponse response = new ApiExceptionResponse("need to login",
                HttpStatus.UNAUTHORIZED,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(
                response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {ProfileNotFoundException.class})
    public ResponseEntity<Object> handleApiRequestException(ProfileNotFoundException exception) {
        ApiExceptionResponse response = new ApiExceptionResponse(exception.getMessage(),
                HttpStatus.UNAUTHORIZED,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(
                response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {ProfileException.class})
    public ResponseEntity<Object> handleApiRequestException(ProfileException exception) {
        ApiExceptionResponse response = new ApiExceptionResponse(exception.getMessage(),
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(
                response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {EmailException.class})
    public ResponseEntity<Object> handleApiRequestException(EmailException exception) {
        ApiExceptionResponse response = new ApiExceptionResponse(exception.getMessage(),
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(
                response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {MailAuthenticationException.class})
    public ResponseEntity<Object> handleApiRequestException(MailAuthenticationException exception) {
        ApiExceptionResponse response = new ApiExceptionResponse(exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(
                response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<Object> handleApiRequestException(IllegalArgumentException exception) {
        ApiExceptionResponse response = new ApiExceptionResponse(exception.getMessage(),
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(
                response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationExceptionResponse> handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> errorMessages = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errorMessages.put(fieldName, errorMessage);
        });

        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z")),
                errorMessages);

        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<Object> handleException(Exception exception) {
        ApiExceptionResponse response = new ApiExceptionResponse(exception.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ZonedDateTime.now(ZoneId.of("Z"))
        );
        return new ResponseEntity<>(
                response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Map<String, List<String>> getErrorsMap(List<String> errors) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", errors);
        return errorResponse;
    }

}