package com.smartlist.api.notification.email;

import com.smartlist.api.notification.email.core.EmailSender;
import com.smartlist.api.notification.email.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private EmailSender emailSender;

    private EmailService emailService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService(emailSender);
    }

    @Test
    void shouldSendPasswordResetEmailSuccessfully() {
        String to = "user@example.com";
        String resetLink = "https://reset.link/token123";

        // Executa o método
        emailService.sendPasswordResetEmail(to, resetLink);

        // Verifica se o emailSender foi chamado com os parâmetros corretos
        verify(emailSender, times(1)).send(
                eq(to),
                eq("Redefinição de Senha"),
                eq("<p>Você solicitou uma redefinição de senha</p>" +
                        "<br>" +
                        "<p>Clique no link para redefinir sua senha: <a href=\"" + resetLink + "\">Redefinir</a></p>")
        );
    }

    @Test
    void shouldSendPasswordResetEmailWithDifferentLink() {
        String to = "another@example.com";
        String resetLink = "https://reset.link/anotherToken";

        emailService.sendPasswordResetEmail(to, resetLink);

        verify(emailSender, times(1)).send(
                eq(to),
                eq("Redefinição de Senha"),
                eq("<p>Você solicitou uma redefinição de senha</p>" +
                        "<br>" +
                        "<p>Clique no link para redefinir sua senha: <a href=\"" + resetLink + "\">Redefinir</a></p>")
        );
    }
}
