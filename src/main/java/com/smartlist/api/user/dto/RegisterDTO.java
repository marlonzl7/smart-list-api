package com.smartlist.api.user.dto;

import com.smartlist.api.user.enums.NotificationPreference;

public record RegisterDTO(String email, String password, String phoneNumber, NotificationPreference notificationPreference) {
}
