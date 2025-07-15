package com.smartlist.api.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(PhoneNumberRequiredException.class)
    public ResponseEntity<ErrorResponse> handlePhoneNumberRequired(PhoneNumberRequiredException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(WeakPasswordException.class)
    public ResponseEntity<ErrorResponse> handleWeakPassword(WeakPasswordException ex) {
        return  ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException invalidFormatEx) {
            if (invalidFormatEx.getPath() != null && !invalidFormatEx.getPath().isEmpty()) {
                String fieldName = invalidFormatEx.getPath().get(0).getFieldName();

                if ("notificationPreference".equals(fieldName)) {
                    return ResponseEntity.badRequest().body(
                        new ErrorResponse("004", "notificationPreference deve ser um dos seguintes valores: EMAIL, WHATSAPP, BOTH")
                    );
                }
            }
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("005", "Requisição mal formatada ou dados inválidos"));
    }

    public ResponseEntity<ErrorResponse> handleInvalidJwt(InvalidJwtException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }
}
