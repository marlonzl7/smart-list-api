package com.smartlist.api.notification.email;

import com.smartlist.api.notification.email.core.EmailSender;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class FakeEmailSender implements EmailSender {

    @Override
    public void send(String to, String subject, String html) {}

}
