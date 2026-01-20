package com.smartlist.api.auth;

import com.smartlist.api.auth.dto.LoginRequest;
import com.smartlist.api.auth.dto.TokenResponse;
import com.smartlist.api.auth.service.AuthService;
import com.smartlist.api.exceptions.InvalidCredentialsException;
import com.smartlist.api.exceptions.InvalidJwtException;
import com.smartlist.api.refreshtoken.model.RefreshToken;
import com.smartlist.api.refreshtoken.repository.RefreshTokenRepository;
import com.smartlist.api.infra.security.JwtUtils;
import com.smartlist.api.user.model.User;
import com.smartlist.api.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private jakarta.servlet.http.HttpServletResponse response;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("test@email.com");
        user.setPassword("encodedPassword");

        loginRequest = new LoginRequest("test@email.com", "password");
    }

    @Test
    void shouldLoginSuccessfully() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.password(), user.getPassword())).thenReturn(true);
        when(jwtUtils.generateAccessToken(user.getEmail())).thenReturn("access-token");
        when(jwtUtils.generateRefreshToken(user.getEmail())).thenReturn("refresh-token");

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("claim-id");
        when(claims.getExpiration()).thenReturn(java.util.Date.from(java.time.Instant.now().plusSeconds(3600)));
        when(jwtUtils.getClaimsFromToken("refresh-token")).thenReturn(claims);

        TokenResponse responseDto = authService.login(loginRequest, response);

        assertEquals("access-token", responseDto.accessToken());
        verify(refreshTokenRepository).save(any());
        verify(response).setHeader(eq(HttpHeaders.SET_COOKIE), contains("refreshToken=refresh-token"));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());

        InvalidCredentialsException ex = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginRequest, response)
        );

        assertEquals("A1001", ex.getCode());
    }

    @Test
    void shouldThrowExceptionWhenPasswordIncorrect() {
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginRequest.password(), user.getPassword())).thenReturn(false);

        InvalidCredentialsException ex = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(loginRequest, response)
        );

        assertEquals("A1001", ex.getCode());
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        Cookie cookie = new Cookie("refreshToken", "old-refresh-token");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old-refresh-token");
        oldToken.setUser(user);
        oldToken.setUsed(false);

        when(jwtUtils.isValidToken("old-refresh-token")).thenReturn(true);
        when(refreshTokenRepository.findByToken("old-refresh-token")).thenReturn(Optional.of(oldToken));

        when(jwtUtils.generateAccessToken(user.getEmail())).thenReturn("new-access-token");
        when(jwtUtils.generateRefreshToken(user.getEmail())).thenReturn("new-refresh-token");

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("claim-id");
        when(claims.getExpiration()).thenReturn(java.util.Date.from(java.time.Instant.now().plusSeconds(3600)));
        when(jwtUtils.getClaimsFromToken(anyString())).thenReturn(claims); // aqui evita o erro

        String newAccessToken = authService.refreshToken(request, response);

        assertEquals("new-access-token", newAccessToken);
        assertTrue(oldToken.isUsed());
        verify(refreshTokenRepository).save(oldToken);
        verify(response).setHeader(eq(HttpHeaders.SET_COOKIE), contains("refreshToken=new-refresh-token"));
    }


    @Test
    void shouldThrowExceptionWhenRefreshTokenMissing() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(null);

        InvalidJwtException ex = assertThrows(
                InvalidJwtException.class,
                () -> authService.refreshToken(request, response)
        );

        assertEquals("A1002", ex.getCode());
    }

    @Test
    void shouldThrowExceptionWhenRefreshTokenReused() {
        Cookie cookie = new Cookie("refreshToken", "old-refresh-token");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        RefreshToken oldToken = new RefreshToken();
        oldToken.setToken("old-refresh-token");
        oldToken.setUser(user);
        oldToken.setUsed(true);

        when(jwtUtils.isValidToken("old-refresh-token")).thenReturn(true);
        when(refreshTokenRepository.findByToken("old-refresh-token")).thenReturn(Optional.of(oldToken));

        InvalidJwtException ex = assertThrows(
                InvalidJwtException.class,
                () -> authService.refreshToken(request, response)
        );

        assertEquals("A1003", ex.getCode());
        verify(refreshTokenRepository).invalidateAllByUser(user);
    }

    @Test
    void shouldLogoutSuccessfully() {
        Cookie cookie = new Cookie("refreshToken", "refresh-token");
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(jwtUtils.isValidToken("refresh-token")).thenReturn(true);

        RefreshToken token = new RefreshToken();
        token.setToken("refresh-token");
        token.setUser(user);
        token.setUsed(false);

        when(refreshTokenRepository.findByToken("refresh-token")).thenReturn(Optional.of(token));

        authService.logout(request, response);

        assertTrue(token.isUsed());
        verify(refreshTokenRepository).save(token);
        verify(response).setHeader(eq("Set-Cookie"), contains("Max-Age=0"));
    }

}
