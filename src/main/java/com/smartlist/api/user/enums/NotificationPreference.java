package com.smartlist.api.user.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum NotificationPreference {
    EMAIL, WHATSAPP, BOTH;

    @JsonCreator
    public static NotificationPreference fromString(String value) {
        try {
            return NotificationPreference.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new IllegalArgumentException("notificationPreference deve ser um dos seguintes valores: EMAIL, WHATSAPP, BOTH");
        }
    }
}
