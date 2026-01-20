package com.smartlist.api.user;

import com.smartlist.api.exceptions.EmailAlreadyExistsException;
import com.smartlist.api.exceptions.PhoneNumberRequiredException;
import com.smartlist.api.exceptions.WeakPasswordException;
import com.smartlist.api.user.dto.UserRegisterRequest;
import com.smartlist.api.user.enums.NotificationPreference;
import com.smartlist.api.user.model.User;
import com.smartlist.api.user.repository.UserRepository;
import com.smartlist.api.user.service.PasswordStrengthService;
import com.smartlist.api.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordStrengthService passwordStrengthService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() {
        UserRegisterRequest dto =
                UserRegisterRequestBuilder.aUser()
                        .withCriticalDays(5)
                        .build();

        when(userRepository.findByEmail(dto.email()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(dto.password()))
                .thenReturn("encodedPassword");

        userService.register(dto);

        verify(passwordStrengthService)
                .validatePasswordStrength(dto.password());

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        UserRegisterRequest dto =
                UserRegisterRequestBuilder.aUser().build();

        when(userRepository.findByEmail(dto.email()))
                .thenReturn(Optional.of(new User()));

        EmailAlreadyExistsException exception =
                assertThrows(EmailAlreadyExistsException.class,
                        () -> userService.register(dto));

        assertEquals("U6001", exception.getCode());

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIsWeak() {
        UserRegisterRequest dto =
                UserRegisterRequestBuilder.aUser()
                        .withPassword("123")
                        .build();

        when(userRepository.findByEmail(dto.email()))
                .thenReturn(Optional.empty());

        doThrow(new WeakPasswordException("S5001", "Senha fraca"))
                .when(passwordStrengthService)
                .validatePasswordStrength(dto.password());

        WeakPasswordException exception =
                assertThrows(WeakPasswordException.class,
                        () -> userService.register(dto));

        assertEquals("S5001", exception.getCode());

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void shouldThrowExceptionWhenWhatsappPreferenceWithoutPhone() {
        UserRegisterRequest dto =
                UserRegisterRequestBuilder.aUser()
                        .withNotification(NotificationPreference.WHATSAPP)
                        .build();

        when(userRepository.findByEmail(dto.email()))
                .thenReturn(Optional.empty());

        PhoneNumberRequiredException exception =
                assertThrows(PhoneNumberRequiredException.class,
                        () -> userService.register(dto));

        assertEquals("U6002", exception.getCode());

        verify(userRepository, never())
                .save(any());
    }

    @Test
    void shouldRegisterUserWithWhatsappWhenPhoneIsProvided() {
        UserRegisterRequest dto =
                UserRegisterRequestBuilder.aUser()
                        .withNotification(NotificationPreference.WHATSAPP)
                        .withPhone("11999999999")
                        .build();

        when(userRepository.findByEmail(dto.email()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(dto.password()))
                .thenReturn("encodedPassword");

        userService.register(dto);

        verify(userRepository)
                .save(any(User.class));
    }

    @Test
    void shouldReturnTrueWhenEmailExists() {
        when(userRepository.findByEmail("test@email.com"))
                .thenReturn(Optional.of(new User()));

        boolean exists =
                userService.existsByEmail("test@email.com");

        assertTrue(exists);
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        when(userRepository.findByEmail("test@email.com"))
                .thenReturn(Optional.empty());

        boolean exists =
                userService.existsByEmail("test@email.com");

        assertFalse(exists);
    }

    @Test
    void shouldFindUserByEmail() {
        User user = new User();
        user.setEmail("test@email.com");

        when(userRepository.findByEmail("test@email.com"))
                .thenReturn(Optional.of(user));

        Optional<User> result =
                userService.findByEmail("test@email.com");

        assertTrue(result.isPresent());
        assertEquals("test@email.com", result.get().getEmail());
    }
}
