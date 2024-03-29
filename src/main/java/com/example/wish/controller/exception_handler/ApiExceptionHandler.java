package com.example.wish.controller.exception_handler;

import com.example.wish.exception.CantCompleteClientRequestException;
import com.example.wish.exception.FieldMatchException;
import com.example.wish.exception.auth.EmailException;
import com.example.wish.exception.auth.TokenException;
import com.example.wish.exception.profile.CurrentProfileNotFoundException;
import com.example.wish.exception.profile.ProfileException;
import com.example.wish.exception.profile.ProfileNotFoundException;
import com.example.wish.exception.wish.ImageException;
import com.example.wish.exception.wish.WishException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class ApiExceptionHandler {

    //for jpeg ang png image
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ValidationExceptionResponse> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(ex.getMessage()));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(value = {WishException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(WishException exception) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(exception.getMessage()));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {ImageException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(ImageException exception) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(exception.getMessage()));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }


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

    @ExceptionHandler(value = {FileSizeLimitExceededException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(FileSizeLimitExceededException exception) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage("Max size limit 10MB. " + exception.getMessage()));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {NumberFormatException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(NumberFormatException exception) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage("failed convert id value of type String to required type long"));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
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

    //ConstraintViolationException
    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<ValidationExceptionResponse> handleApiRequestException(ConstraintViolationException ex) {
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

    @ExceptionHandler(FieldMatchException.class)
    public ResponseEntity<ValidationExceptionResponse> handleFieldMatchException(FieldMatchException ex) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(ex.getMessage()));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ValidationException .class)
    public ResponseEntity<ValidationExceptionResponse> handleFieldMatchException(ValidationException ex) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(ex.getMessage()));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<ValidationExceptionResponse> handleException(Exception ex) {
        ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage(ex.getMessage()));
        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationExceptionResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        Throwable rootCause = getRootCause(ex);

        if (rootCause instanceof FieldMatchException) {
            FieldMatchException fieldMatchException = (FieldMatchException) rootCause;
            ValidationExceptionResponse errorResponse = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST, ZonedDateTime.now(ZoneId.of("Z")),
                    createErrorMessage(fieldMatchException.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        if (rootCause instanceof InvalidFormatException) {
            InvalidFormatException invalidFormatException = (InvalidFormatException) rootCause;
            ValidationExceptionResponse response = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST,
                    ZonedDateTime.now(ZoneId.of("Z")),
                    createErrorMessage("invalid format value. "+ invalidFormatException.getMessage()));
            return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
        }

        // Default error response for other cases
        ValidationExceptionResponse errorResponse = new ValidationExceptionResponse(HttpStatus.BAD_REQUEST, ZonedDateTime.now(ZoneId.of("Z")),
                createErrorMessage("Invalid request payload"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Helper method to get the root cause of an exception
    private Throwable getRootCause(Throwable ex) {
        Throwable rootCause = ex;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationExceptionResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errorMessages = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            if (error instanceof FieldError) {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errorMessages.put(fieldName, errorMessage);
            } else if (error instanceof ObjectError) {
                String objectName = ((ObjectError) error).getObjectName();
                String errorMessage = error.getDefaultMessage();
                errorMessages.put(objectName, errorMessage);
            }
        });

        ValidationExceptionResponse response = new ValidationExceptionResponse(
                HttpStatus.BAD_REQUEST,
                ZonedDateTime.now(ZoneId.of("Z")),
                errorMessages
        );

        return new ResponseEntity<>(response, new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }


    private Map<String, String> createErrorMessage(String message) {
        Map<String, String> errorMessages = new HashMap<>();
        errorMessages.put("error_message", message);
        return errorMessages;
    }
}
