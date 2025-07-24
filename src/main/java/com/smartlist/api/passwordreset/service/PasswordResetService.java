package com.smartlist.api.passwordreset.service;

import com.smartlist.api.config.PasswordResetProperties;
import com.smartlist.api.config.RateLimitProperties;
import com.smartlist.api.email.service.EmailService;
import com.smartlist.api.exceptions.BadRequestException;
import com.smartlist.api.passwordreset.dto.PasswordExchangeDTO;
import com.smartlist.api.passwordreset.dto.PasswordResetRequestDTO;
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

    public void requestPasswordReset(PasswordResetRequestDTO passwordResetRequestDTO) {
        log.info("Iniciando requisição de redefinição de senha para email: {}", passwordResetRequestDTO.email());

        validateRequestRateLimit(passwordResetRequestDTO.email(), passwordResetRequestDTO.requestIp());

        var userOptional = userService.findByEmail(passwordResetRequestDTO.email());

        if (userOptional.isEmpty()) {
            log.warn("Tentativa de redefinição de senha com email não registrado: {}", passwordResetRequestDTO.email());
            return;
        }

        var user = userOptional.get();

        PasswordResetToken passwordResetToken = generateAndSaveToken(user, passwordResetRequestDTO);
        log.debug("Token de redefinição gerado com sucesso para usuário com ID: {}", user.getUserId());

        String resetLink = passwordResetLink + passwordResetToken.getToken();
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);

        log.info("Email de redefinição de senha enviado para: {}", user.getEmail());
    }

    private void validateRequestRateLimit(String email, String requestIp) {
        log.debug("Validando rate limit para email: {} e IP: {}", email, requestIp);
        Instant now = Instant.now();
        Instant windowStart = now.minusSeconds(rateLimitDurationSeconds);

        long emailCount = passwordResetTokenRepository.countByEmailAndCreatedAtAfter(email, windowStart);
        if (emailCount >= maxRequestsPerEmail) {
            log.warn("Rate limit excedido para email: {}. Requisições recentes: {}", email, emailCount);
            throw new BadRequestException("017", "Muitas requisições de redefinição de senha com esse e-mail. Tente novamente em alguns minutos.");
        }

        validateRequestIp(requestIp, windowStart);
    }

    private void validateRequestIp(String requestIp, Instant windowStart) {
        log.debug("Validando IP de origem: {}", requestIp);

        InetAddressValidator ipValidator = InetAddressValidator.getInstance();

        if (!ipValidator.isValid(requestIp)) {
            log.warn("IP de origem inválido: {}", requestIp);
            throw new BadRequestException("012", "Ip de origem inválido");
        }

        long ipCount = passwordResetTokenRepository.countByRequestIpAndCreatedAtAfter(requestIp, windowStart);
        if (ipCount >= maxRequestsPerIp) {
            log.warn("Rate limit excedido para IP: {}. Requisições recentes: {}", requestIp, ipCount);
            throw new BadRequestException("018", "Muitas requisições de redefinição de senha com esse IP. Tente novamente em alguns minutos.");
        }
    }

    private PasswordResetToken generateAndSaveToken(User user, PasswordResetRequestDTO dto) {
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

        return saved;
    }

    private PasswordResetToken getValidToken(UUID token) {
        log.debug("Validando token de redefinição de senha recebido: {}", token);
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token).orElseThrow(() -> {
            log.warn("Token de redefinição de senha não encontrado: {}", token);
            return new BadRequestException("013", "Token não encontrado");
        });

        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            log.warn("Token expirado: {}", token);
            resetToken.setStatus(PasswordResetTokenStatus.EXPIRED);
            passwordResetTokenRepository.save(resetToken);
            throw new BadRequestException("014", "Token expirado");
        }

        if (resetToken.getStatus() != PasswordResetTokenStatus.PENDING) {
            log.warn("Token inválido ou já utilizado: {}. Status atual: {}", token, resetToken.getStatus());
            throw new BadRequestException("015", "Token inválido ou já utilizado");
        }

        log.debug("Token validado com sucesso: {}", token);
        return resetToken;
    }

    public boolean validateToken(UUID token) {
        log.debug("Verificando validade do token: {}", token);
        getValidToken(token);
        return true;
    }

    public void resetPassword(PasswordExchangeDTO passwordExchangeDTO) {
        log.info("Redefinindo senha para token: {}", passwordExchangeDTO.token());
        PasswordResetToken resetToken = getValidToken(passwordExchangeDTO.token());

        String email = resetToken.getEmail();
        log.debug("Buscando usuário com email associado ao token: {}", email);

        User user = userRepository.findByEmail(email).orElseThrow(() -> {
            log.error("Usuário não encontrado com email: {}", email);
            return new BadRequestException("016", "Usuário não encontrado");
        });

        log.debug("Validando força da nova senha para o usuário ID: {}", user.getUserId());
        passwordStrengthService.validatePasswordStrength(passwordExchangeDTO.newPassword());

        resetToken.setStatus(PasswordResetTokenStatus.USED);
        passwordResetTokenRepository.save(resetToken);
        log.debug("Token de reset marcado como usado: {}", resetToken.getToken());

        user.setPassword(passwordEncoder.encode(passwordExchangeDTO.newPassword()));
        userRepository.save(user);

        log.info("Senha redefinida com sucesso para o usuário ID: {} (email: {})", user.getUserId(), user.getEmail());
    }

    public UUID generateResetToken() {
        return UUID.randomUUID();
    }
}
