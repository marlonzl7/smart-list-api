package com.smartlist.api.user.service;

import com.smartlist.api.exceptions.EmailAlreadyExistsException;
import com.smartlist.api.exceptions.PhoneNumberRequiredException;
import com.smartlist.api.user.dto.RegisterDTO;
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

    public void register(RegisterDTO dto) {
        log.info("Iniciado cadastro de usuário para email: {}", dto.email());

        if (existsByEmail(dto.email())) {
            log.error("Tentativa de cadastro com email já registrado. Email: {}", dto.email());
            throw new EmailAlreadyExistsException("U6001", "Email já em uso.");
        }

        log.debug("Validando força de senha para cadastro do usuário email: {}", dto.email());
        passwordStrengthService.validatePasswordStrength(dto.password());

        if (
            (dto.notificationPreference() == NotificationPreference.WHATSAPP ||
             dto.notificationPreference() == NotificationPreference.BOTH) &&
            (dto.phoneNumber() == null || dto.phoneNumber().isEmpty())
        ) {
            log.error("Número de telefone omitido na tentativa de cadastro com a opção de notificação de preferência WHATSAPP ou BOTH. Email: {}", dto.email());
            throw new PhoneNumberRequiredException("U6002", "Número de telefone é obrigatório para notificações via Whatsapp ou ambas.");
        }

        User user = new User();
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setNotificationPreference(dto.notificationPreference());
        user.setThemePreference(ThemePreference.SYSTEM);

        if (dto.phoneNumber() != null && !dto.phoneNumber().isEmpty()) {
            user.setPhoneNumber(dto.phoneNumber());
        }

        userRepository.save(user);

        log.info("Usuário cadastrado com sucesso. Email {}", user.getEmail());
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
