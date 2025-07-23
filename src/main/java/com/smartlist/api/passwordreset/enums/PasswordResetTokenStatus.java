package com.smartlist.api.passwordreset.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.smartlist.api.user.enums.NotificationPreference;

public enum PasswordResetTokenStatus {
    PENDING, USED, EXPIRED;

    @JsonCreator
    public static PasswordResetTokenStatus fromString(String value) {
        try {
            return PasswordResetTokenStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException("Status do token de redefinição de senha deve ser um dos seguintes valores: PENDING, USED, EXPIRED");
        }
    }
}
