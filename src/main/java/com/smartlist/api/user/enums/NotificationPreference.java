package com.smartlist.api.user.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.smartlist.api.exceptions.BadRequestException;

public enum NotificationPreference {
    EMAIL, WHATSAPP, BOTH;

    @JsonCreator
    public static NotificationPreference fromString(String value) {
        try {
            return NotificationPreference.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BadRequestException("ENP1001", "A notificação de preferência deve ser um dos seguintes valores: EMAIL, WHATSAPP, BOTH");
        }
    }
}
