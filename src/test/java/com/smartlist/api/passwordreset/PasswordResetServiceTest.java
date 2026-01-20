package com.smartlist.api.passwordreset;

import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.infra.config.PasswordResetProperties;
import com.smartlist.api.infra.config.RateLimitProperties;
import com.smartlist.api.notification.email.service.EmailService;
import com.smartlist.api.passwordreset.dto.PasswordExchangeRequest;
import com.smartlist.api.passwordreset.dto.PasswordResetRequest;
import com.smartlist.api.passwordreset.enums.PasswordResetTokenStatus;
import com.smartlist.api.passwordreset.model.PasswordResetToken;
import com.smartlist.api.passwordreset.repository.PasswordResetTokenRepository;
import com.smartlist.api.passwordreset.service.PasswordResetService;
import com.smartlist.api.user.model.User;
import com.smartlist.api.user.repository.UserRepository;
import com.smartlist.api.user.service.PasswordStrengthService;
import com.smartlist.api.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private PasswordStrengthService passwordStrengthService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        PasswordResetProperties passwordResetProperties = new PasswordResetProperties();
        passwordResetProperties.setExpirationSeconds(3600L);
        passwordResetProperties.setLink("https://reset.link/");

        RateLimitProperties rateLimitProperties = new RateLimitProperties();
        rateLimitProperties.setEmail(5);
        rateLimitProperties.setIp(10);
        rateLimitProperties.setDuration(60);

        passwordResetService = new PasswordResetService(
                passwordResetTokenRepository,
                userService,
                emailService,
                passwordStrengthService,
                passwordEncoder,
                userRepository,
                passwordResetProperties,
                rateLimitProperties
        );
    }

    @Test
    void shouldResetPasswordSuccessfully() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUserId(1L);

        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID());
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        token.setStatus(PasswordResetTokenStatus.PENDING);
        token.setUser(user);
        token.setEmail(user.getEmail());

        when(passwordResetTokenRepository.findByToken(token.getToken()))
                .thenReturn(Optional.of(token));

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");

        PasswordExchangeRequest dto = new PasswordExchangeRequest(token.getToken(), "newPassword");

        passwordResetService.resetPassword(dto);

        verify(passwordStrengthService, times(1)).validatePasswordStrength("newPassword");
        assertEquals(PasswordResetTokenStatus.USED, token.getStatus());
    }

    @Test
    void shouldThrowWhenTokenExpired() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID());
        token.setExpiresAt(Instant.now().minusSeconds(10)); // expirado
        token.setStatus(PasswordResetTokenStatus.PENDING);

        when(passwordResetTokenRepository.findByToken(token.getToken()))
                .thenReturn(Optional.of(token));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> passwordResetService.getValidToken(token.getToken()));

        assertEquals("P3005", exception.getCode());
        assertEquals(PasswordResetTokenStatus.EXPIRED, token.getStatus());
    }

    @Test
    void shouldReturnValidTokenSuccessfully() {
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(UUID.randomUUID());
        token.setExpiresAt(Instant.now().plusSeconds(3600)); // futuro
        token.setStatus(PasswordResetTokenStatus.PENDING);

        when(passwordResetTokenRepository.findByToken(token.getToken()))
                .thenReturn(Optional.of(token));

        PasswordResetToken validToken = passwordResetService.getValidToken(token.getToken());

        assertEquals(token.getToken(), validToken.getToken());
    }

}
