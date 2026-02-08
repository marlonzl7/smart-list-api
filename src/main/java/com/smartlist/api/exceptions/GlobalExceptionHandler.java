package com.smartlist.api.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.smartlist.api.shared.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), ex.getCode());
    }

    @ExceptionHandler(PhoneNumberRequiredException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handlePhoneNumberRequired(PhoneNumberRequiredException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), ex.getCode());
    }

    @ExceptionHandler(WeakPasswordException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleWeakPassword(WeakPasswordException ex) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), ex.getCode());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleBadRequest(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), ex.getCode());
    }

    @ExceptionHandler(InvalidJwtException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleInvalidJwt(InvalidJwtException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex.getCode());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleInvalidCredentials(InvalidCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), ex.getCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Erro de validação na requisição: {}", errors);

        return ResponseEntity
                .badRequest()
                .body(new ApiResponse<>(false, "Erro de validação", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMalformedJson(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        Map<String, String> errorData = new HashMap<>();

        if (cause instanceof InvalidFormatException invalidFormatEx &&
                invalidFormatEx.getPath() != null &&
                !invalidFormatEx.getPath().isEmpty()) {

            String fieldName = invalidFormatEx.getPath().get(0).getFieldName();

            if ("notificationPreference".equals(fieldName)) {
                log.warn("Valor inválido para notificationPreference");
                errorData.put("code", "004");

                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(
                                false,
                                "notificationPreference deve ser um dos seguintes valores: EMAIL, WHATSAPP, BOTH",
                                errorData
                        )
                );
            }
        }

        log.warn("Requisição mal formatada: {}", ex.getMessage());
        errorData.put("code", "005");

        return ResponseEntity
                .badRequest()
                .body(new ApiResponse<>(false, "Requisição mal formatada ou dados inválidos", errorData));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Parâmetro obrigatório ausente: {}", ex.getParameterName());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: " + ex.getParameterName(),
                "400"
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Tipo inválido para parâmetro '{}'", ex.getName());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Valor inválido para o parâmetro: " + ex.getName(),
                "400"
        );
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleNotFound(NoHandlerFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Recurso não encontrado", "404");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                "Método HTTP não permitido para este recurso",
                "405"
        );
    }

    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleEmailSendException(EmailSendException ex) {
        log.error("Erro ao enviar email", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex.getCode());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleUnexpected(Exception ex) {
        log.error("Erro inesperado não tratado", ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno no servidor. Tente novamente mais tarde.",
                "500"
        );
    }

    private ResponseEntity<ApiResponse<Map<String, String>>> buildResponse(
            HttpStatus status,
            String message,
            String code
    ) {
        Map<String, String> errorData = Map.of("code", code);
        return ResponseEntity
                .status(status)
                .body(new ApiResponse<>(false, message, errorData));
    }
}
