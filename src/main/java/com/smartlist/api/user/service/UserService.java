package com.smartlist.api.user.service;

import com.smartlist.api.exceptions.EmailAlreadyExistsException;
import com.smartlist.api.exceptions.PhoneNumberRequiredException;
import com.smartlist.api.user.dto.RegisterDTO;
import com.smartlist.api.user.enums.NotificationPreference;
import com.smartlist.api.user.enums.ThemePreference;
import com.smartlist.api.user.model.User;
import com.smartlist.api.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        if (existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("001", "Email já em uso.");
        }

        passwordStrengthService.validatePasswordStrength(dto.password());

        if (
            (dto.notificationPreference() == NotificationPreference.WHATSAPP ||
             dto.notificationPreference() == NotificationPreference.BOTH) &&
            (dto.phoneNumber() == null || dto.phoneNumber().isEmpty())
        ) {
            throw new PhoneNumberRequiredException("002", "Número de telefone é obrigatório para notificações via Whatsapp ou ambas.");
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
    }

    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
