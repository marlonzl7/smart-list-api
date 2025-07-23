package com.smartlist.api.email.core;

public interface EmailSender {
    void send(String to, String subject, String html);
}
