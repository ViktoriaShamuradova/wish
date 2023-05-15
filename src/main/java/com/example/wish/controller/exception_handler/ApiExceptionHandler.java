package com.example.wish.controller.exception_handler;

import com.example.wish.exception.CantCompleteClientRequestException;
import com.example.wish.exception.auth.EmailException;
import com.example.wish.exception.auth.TokenException;
import com.example.wish.exception.profile.CurrentProfileNotFoundException;
import com.example.wish.exception.profile.ProfileException;
import com.example.wish.exception.profile.ProfileNotFoundException;
import com.example.wish.exception.wish.WishException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(value = {WishException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(WishException exception) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(exception.getMessage()));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    //catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | SignatureException e) {
    //            log.error("Error logging in {}", e.getMessage());
    //            Map<String, String> errors = new HashMap<>();
    //            errors.put("token_error", e.getMessage());
    //            handleException(response, errors);
    //   response.setContentType("application/json");
    //        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    //        new ObjectMapper().writeValue(response.getOutputStream(), errors);
    @ExceptionHandler(ExpiredJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleExpiredJwtException(ExpiredJwtException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("token_error", ex.getMessage());
        return errors;
    }

    @ExceptionHandler(UnsupportedJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleUnsupportedJwtException(UnsupportedJwtException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("token_error", ex.getMessage());
        return errors;
    }

    @ExceptionHandler(MalformedJwtException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleUnsupportedJwtException(MalformedJwtException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("token_error", ex.getMessage());
        return errors;
    }

    @ExceptionHandler(SignatureException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleUnsupportedJwtException(SignatureException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put("token_error", ex.getMessage());
        return errors;
    }

    //это исключение выбрасыватся может для энамов, если значения указаны не верно, подкорректировать message
    @ExceptionHandler(value = {InvalidFormatException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(InvalidFormatException exception) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage("invalid format value"));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    /**
     * выбрасывается если не сгенерировался uid или токен, если отсувствует сообщение при отправке сообщения на почту
     *
     * @param exception
     * @return
     */
    @ExceptionHandler(value = {CantCompleteClientRequestException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(CantCompleteClientRequestException exception) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage("cant complete client request"));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {TokenException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(TokenException ex) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.UNAUTHORIZED,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(ex.getMessage()));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {CurrentProfileNotFoundException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(CurrentProfileNotFoundException exception) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.UNAUTHORIZED,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage("need to login"));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {ProfileNotFoundException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(ProfileNotFoundException ex) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.UNAUTHORIZED,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(ex.getMessage()));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(value = {ProfileException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(ProfileException ex) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(ex.getMessage()));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {EmailException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(EmailException ex) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(ex.getMessage()));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {MailAuthenticationException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(MailAuthenticationException ex) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(ex.getMessage()));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(IllegalArgumentException ex) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(ex.getMessage()));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ValidationExceptionResponse> handleException(Exception ex) {

        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(ex.getMessage()));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
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

    private Map<String, String> createErrorMessage(String message) {
        Map<String, String> errorMessages = new HashMap<>();
        errorMessages.put("error_message", message);
        return errorMessages;
    }
}
