package com.smartlist.api.passwordreset.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank(message = "Email é obrigatório.")
        @Email(message = "Email inválido.")
        String email,

        @NotBlank(message = "IP de origem é obrigatório.")
        String requestIp,

        @NotBlank(message = "User-Agent é obrigatório.")
        String userAgent
) {}
