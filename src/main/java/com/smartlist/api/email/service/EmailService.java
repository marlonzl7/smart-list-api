package com.smartlist.api.email.service;

import com.smartlist.api.email.core.EmailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final EmailSender emailSender;

    public EmailService(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        String subject = "Redefinição de Senha";
        String html = "<p>Você solicitou uma redefinição de senha</p>" +
                "<br>" +
                "<p>Clique no link para redefinir sua senha: <a href=\"" + resetLink + "\">Redefinir</a></p>";
        emailSender.send(to, subject, html);
    }
}
