package com.smartlist.api.user.service;

import com.smartlist.api.exceptions.EmailAlreadyExistsException;
import com.smartlist.api.exceptions.PhoneNumberRequiredException;
import com.smartlist.api.user.dto.UserRegisterRequest;
import com.smartlist.api.user.enums.NotificationPreference;
import com.smartlist.api.user.enums.ThemePreference;
import com.smartlist.api.user.model.User;
import com.smartlist.api.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UserService {
    private final PasswordStrengthService passwordStrengthService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(PasswordStrengthService passwordStrengthService, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordStrengthService = passwordStrengthService;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public void register(UserRegisterRequest dto) {
        log.info("Tentativa de cadastro de usuário iniciada");

        if (existsByEmail(dto.email())) {
            log.warn(
                    "Tentativa de cadastro com email já existente. Email={}",
                    dto.email()
            );
            throw new EmailAlreadyExistsException("U6001", "Email já em uso.");
        }

        log.debug("Validando força de senha para cadastro do usuário email: {}", dto.email());
        passwordStrengthService.validatePasswordStrength(dto.password());

        if (
            (dto.notificationPreference() == NotificationPreference.WHATSAPP ||
             dto.notificationPreference() == NotificationPreference.BOTH) &&
            (dto.phoneNumber() == null || dto.phoneNumber().isEmpty())
        ) {
            log.warn(
                    "Número de telefone omitido para preferência de notificação WHATSAPP/BOTH. Email={}",
                    dto.email()
            );
            throw new PhoneNumberRequiredException("U6002", "Número de telefone é obrigatório para notificações via Whatsapp ou ambas.");
        }

        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setNotificationPreference(dto.notificationPreference()); // adicionar validação aqui depois
        user.setThemePreference(ThemePreference.SYSTEM);

        if (dto.phoneNumber() != null && !dto.phoneNumber().isEmpty()) {
            user.setPhoneNumber(dto.phoneNumber());
        }

        if (dto.criticalQuantityDays() != null) {
            user.setCriticalQuantityDays(dto.criticalQuantityDays());
        }

        userRepository.save(user);

        log.info("Usuário cadastrado com sucesso. UserId={}, Email={}", user.getUserId(), user.getEmail());
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
