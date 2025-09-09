package com.smartlist.api.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.smartlist.api.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        Map<String, String> errorData = Map.of("code", ex.getCode());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiResponse<>(false, ex.getMessage(), errorData));
    }

    @ExceptionHandler(PhoneNumberRequiredException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handlePhoneNumberRequired(PhoneNumberRequiredException ex) {
        Map<String, String> errorData = Map.of("code", ex.getCode());
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiResponse<>(false, ex.getMessage(), errorData));
    }

    @ExceptionHandler(WeakPasswordException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleWeakPassword(WeakPasswordException ex) {
        Map<String, String> errorData = Map.of("code", ex.getCode());
        return  ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiResponse<>(false, ex.getMessage(), errorData));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Falha na validação de campos da requisição: {}", errors);

        return ResponseEntity.badRequest().body(new ApiResponse<>(false, "Erro de validação", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleHttpNotReadable(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        Map<String, String> errorData = new HashMap<>();

        if (cause instanceof InvalidFormatException invalidFormatEx) {
            if (invalidFormatEx.getPath() != null && !invalidFormatEx.getPath().isEmpty()) {
                String fieldName = invalidFormatEx.getPath().get(0).getFieldName();

                if ("notificationPreference".equals(fieldName)) {
                    log.warn("Valor inválido para notificationPreference");
                    errorData.put("code", "004");

                    return ResponseEntity.badRequest().body(
                            new ApiResponse<>(false,
                                    "notificationPreference deve ser um dos seguintes valores: EMAIL, WHATSAPP, BOTH",
                                    errorData)
                    );
                }
            }
        }

        log.warn("Requisição mal formatada: {}", ex.getMessage());
        errorData.put("code", "005");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, "Requisição mal formatada ou dados inválidos", errorData));
    }

    @ExceptionHandler(InvalidJwtException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleInvalidJwt(InvalidJwtException ex) {
        Map<String, String> errorData = Map.of("code", ex.getCode());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, ex.getMessage(), errorData));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleInvalidCredentials(InvalidCredentialsException ex) {
        Map<String, String> errorData = Map.of("code", ex.getCode());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse<>(false, ex.getMessage(), errorData));
    }

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleEmailSendException(EmailSendException ex) {
        log.error("Erro ao enviar email: {}", ex.getMessage(), ex);
        Map<String, String> errorData = Map.of("code", ex.getCode());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, ex.getMessage(), errorData));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBadRequestException(BadRequestException ex) {
        Map<String, String> errorData = Map.of("code", ex.getCode());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(false, ex.getMessage(), errorData));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleUnexpected(Exception ex) {
        log.error("Erro inesperado não tratado: {}", ex.getMessage(), ex);
        Map<String, String> errorData = Map.of("code", "500");
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Erro interno no servidor. Tente novamente mais tarde.", errorData));
    }
}
