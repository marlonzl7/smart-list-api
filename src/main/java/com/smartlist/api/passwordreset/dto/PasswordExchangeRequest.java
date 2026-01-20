package com.smartlist.api.passwordreset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PasswordExchangeRequest(
        @NotNull(message = "Esperava um token.")
        UUID token,

        @NotBlank(message = "Senha é obrigatória.")
        String newPassword
) {}
