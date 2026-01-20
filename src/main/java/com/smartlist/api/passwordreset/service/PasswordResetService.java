package com.smartlist.api.passwordreset.service;

import com.smartlist.api.infra.config.PasswordResetProperties;
import com.smartlist.api.infra.config.RateLimitProperties;
import com.smartlist.api.notification.email.service.EmailService;
import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.passwordreset.dto.PasswordExchangeRequest;
import com.smartlist.api.passwordreset.dto.PasswordResetRequest;
import com.smartlist.api.passwordreset.enums.PasswordResetTokenStatus;
import com.smartlist.api.passwordreset.model.PasswordResetToken;
import com.smartlist.api.passwordreset.repository.PasswordResetTokenRepository;
import com.smartlist.api.user.model.User;
import com.smartlist.api.user.repository.UserRepository;
import com.smartlist.api.user.service.PasswordStrengthService;
import com.smartlist.api.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class PasswordResetService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final PasswordStrengthService passwordStrengthService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    private final long tokenExpirationSeconds; // 10 min
    private final String passwordResetLink;
    private final int maxRequestsPerEmail;
    private final int maxRequestsPerIp;
    private final int rateLimitDurationSeconds;

    public PasswordResetService(
            PasswordResetTokenRepository passwordResetTokenRepository,
            UserService userService,
            EmailService emailService,
            PasswordStrengthService passwordStrengthService,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository,
            PasswordResetProperties passwordResetProperties,
            RateLimitProperties rateLimitProperties
    ) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userService = userService;
        this.emailService = emailService;
        this.passwordStrengthService = passwordStrengthService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.tokenExpirationSeconds = passwordResetProperties.getExpirationSeconds();
        this.passwordResetLink = passwordResetProperties.getLink();
        this.maxRequestsPerEmail = rateLimitProperties.getEmail();
        this.maxRequestsPerIp = rateLimitProperties.getIp();
        this.rateLimitDurationSeconds = rateLimitProperties.getDuration();
    }

    public void requestPasswordReset(PasswordResetRequest dto) {
        log.info("Requisição de redefinição de senha recebida");

        validateRequestRateLimit(dto.email(), dto.requestIp());

        var userOptional = userService.findByEmail(dto.email());

        if (userOptional.isEmpty()) {
            log.warn("Tentativa de redefinição de senha para email inexistente");
            return;
        }

        var user = userOptional.get();

        PasswordResetToken passwordResetToken = generateAndSaveToken(user, dto);
        log.debug("Token de redefinição gerado com sucesso para usuário com ID: {}", user.getUserId());

        String resetLink = passwordResetLink + passwordResetToken.getToken();
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

        log.info("Email de redefinição de senha enviado com sucesso");
    }

    private void validateRequestRateLimit(String email, String requestIp) {
        log.debug("Validando rate limit para email: {} e IP: {}", email, requestIp);
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(rateLimitDurationSeconds);

        long emailCount = passwordResetTokenRepository.countByEmailAndCreatedAtAfter(email, windowStart);
        if (emailCount >= maxRequestsPerEmail) {
            log.warn("Rate limit excedido para email: {}. Requisições recentes: {}", email, emailCount);
            throw new BadRequestException("P3001", "Muitas requisições de redefinição de senha com esse e-mail. Tente novamente em alguns minutos.");
        }

        validateRequestIp(requestIp, windowStart);
    }

    private void validateRequestIp(String requestIp, Instant windowStart) {
        log.debug("Validando IP de origem: {}", requestIp);

        InetAddressValidator ipValidator = InetAddressValidator.getInstance();

        if (!ipValidator.isValid(requestIp)) {
            log.warn("IP de origem inválido: {}", requestIp);
            throw new BadRequestException("P3002", "Ip de origem inválido");
        }

        long ipCount = passwordResetTokenRepository.countByRequestIpAndCreatedAtAfter(requestIp, windowStart);
        if (ipCount >= maxRequestsPerIp) {
            log.warn("Rate limit excedido para IP: {}. Requisições recentes: {}", requestIp, ipCount);
            throw new BadRequestException("P3003", "Muitas requisições de redefinição de senha com esse IP. Tente novamente em alguns minutos.");
        }
    }

    private PasswordResetToken generateAndSaveToken(User user, PasswordResetRequest dto) {
        UUID token = generateResetToken();
        Instant expiresAt = Instant.now().plusSeconds(tokenExpirationSeconds);

        log.debug("Gerando token de redefinição de senha para usuário ID: {}", user.getUserId());

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setEmail(user.getEmail());
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiresAt(expiresAt);
        passwordResetToken.setStatus(PasswordResetTokenStatus.PENDING);
        passwordResetToken.setRequestIp(dto.requestIp());
        passwordResetToken.setUserAgent(dto.userAgent());

        PasswordResetToken saved = passwordResetTokenRepository.save(passwordResetToken);
        log.debug("Token salvo no banco para usuário ID: {}", user.getUserId());

        passwordResetTokenRepository.expireAllPendingByUser(user);
        log.debug("Token anteriores invelidados para usuário ID: {}", user.getUserId());

        return saved;
    }

    private PasswordResetToken getValidToken(UUID token) {
        log.debug("Validando token de redefinição de senha recebido");
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElseThrow(() -> {
            log.warn("Token de redefinição de senha não encontrado");
            return new BadRequestException("P3004", "Token não encontrado");
        });

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Token de redefinição expirado.");
            resetToken.setStatus(PasswordResetTokenStatus.EXPIRED);
            passwordResetTokenRepository.save(resetToken);
            throw new BadRequestException("P3005", "Token expirado");
        }

        if (resetToken.getStatus() != PasswordResetTokenStatus.PENDING) {
            log.warn("Token inválido ou já utilizado. Status atual.");
            throw new BadRequestException("P3006", "Token inválido ou já utilizado");
        }

        log.debug("Token validado com sucesso.");
        return resetToken;
    }

    public void resetPassword(PasswordExchangeRequest dto) {
        log.info("Processo de redefinição de senha iniciado");
        PasswordResetToken resetToken = getValidToken(dto.token());

        String email = resetToken.getEmail();
        log.debug("Buscando usuário com email associado ao token: {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("Usuário não encontrado com email: {}", email);
            return new BadRequestException("P3007", "Usuário não encontrado");
        });

        log.debug("Validando força da nova senha para o usuário ID: {}", user.getUserId());
        passwordStrengthService.validatePasswordStrength(dto.newPassword());

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);

        resetToken.setStatus(PasswordResetTokenStatus.USED);
        passwordResetTokenRepository.save(resetToken);
        log.debug("Token de redefinição marcado como usado");

        log.info("Senha redefinida com sucesso para o usuário ID: {}", user.getUserId());
    }

    public UUID generateResetToken() {
        return UUID.randomUUID();
    }
}
