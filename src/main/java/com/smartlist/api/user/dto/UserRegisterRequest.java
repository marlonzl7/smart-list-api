package com.smartlist.api.user.dto;

import com.smartlist.api.user.enums.NotificationPreference;
import jakarta.validation.constraints.*;

public record UserRegisterRequest(
        @NotBlank(message = "Email é obrigatório.")
        @Email(message = "Email inválido.")
        String email,

        @NotBlank(message = "Senha é obrigatória.")
        String password,

        @Pattern(
                regexp = "^\\(?\\d{2}\\)?\\s?9\\d{4}-\\d{4}$",
                message = "Número de telefone inválido. Ex: (11) 99999-9999"
        )
        String phoneNumber,

        @NotNull(message = "Preferência de notificação é obrigatória.")
        NotificationPreference notificationPreference,

        @Min(value = 0, message = "Dias críticos não pode ser negativo.")
        Integer criticalQuantityDays
)
{}
