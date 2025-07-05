package com.smartlist.api.user;

public record RegisterDTO(String email, String password, String phoneNumber, NotificationPreference notificationPreference) {
}
