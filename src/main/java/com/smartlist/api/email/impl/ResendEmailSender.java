package com.smartlist.api.email.impl;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.smartlist.api.email.core.EmailSender;
import com.smartlist.api.exceptions.EmailSendException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("resend")
public class ResendEmailSender implements EmailSender {

    private final Resend resend;

    @Value("${resend.from.email}")
    private String from;

    public ResendEmailSender(Resend resend) {
        this.resend = resend;
    }

    @Override
    public void send(String to, String subject, String html) {
        CreateEmailOptions emailOptions = CreateEmailOptions.builder()
                .from(from)
                .to(to)
                .subject(subject)
                .html(html)
                .build();

        try {
            resend.emails().send(emailOptions);
        } catch (ResendException e) {
            throw new EmailSendException("011", "Erro ao enviar email");
        }
    }
}
