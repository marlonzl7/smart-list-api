package com.smartlist.api.notification.email.core;

public interface EmailSender {
    void send(String to, String subject, String html);
}
