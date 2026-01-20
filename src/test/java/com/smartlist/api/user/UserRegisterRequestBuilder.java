package com.smartlist.api.user;

import com.smartlist.api.user.dto.UserRegisterRequest;
import com.smartlist.api.user.enums.NotificationPreference;

public class UserRegisterRequestBuilder {

    private String email = "test@email.com";
    private String password = "StrongPassword123!";
    private String phoneNumber = null;
    private NotificationPreference notificationPreference = NotificationPreference.EMAIL;
    private Integer criticalQuantityDays = null;

    public static UserRegisterRequestBuilder aUser() {
        return new UserRegisterRequestBuilder();
    }

    public UserRegisterRequestBuilder withPhone(String phone) {
        this.phoneNumber = phone;
        return this;
    }

    public UserRegisterRequestBuilder withNotification(NotificationPreference pref) {
        this.notificationPreference = pref;
        return this;
    }

    public UserRegisterRequestBuilder withCriticalDays(Integer days) {
        this.criticalQuantityDays = days;
        return this;
    }

    public UserRegisterRequest build() {
        return new UserRegisterRequest(
                email,
                password,
                phoneNumber,
                notificationPreference,
                criticalQuantityDays
        );
    }

    public UserRegisterRequestBuilder withPassword(String password) {
        this.password = password;
        return this;
    }

}
