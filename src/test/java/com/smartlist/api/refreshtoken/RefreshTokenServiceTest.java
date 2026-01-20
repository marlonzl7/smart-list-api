package com.smartlist.api.refreshtoken;

import com.smartlist.api.refreshtoken.model.RefreshToken;
import com.smartlist.api.refreshtoken.repository.RefreshTokenRepository;
import com.smartlist.api.refreshtoken.service.RefreshTokenService;
import com.smartlist.api.user.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    private RefreshTokenRepository refreshTokenRepository;
    private RefreshTokenService refreshTokenService;
    private User user;

    @BeforeEach
    void setup() {
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        refreshTokenService = new RefreshTokenService(refreshTokenRepository);

        user = new User();
        user.setUserId(1L);
        user.setEmail("test@email.com");
    }

    @Test
    void shouldReturnEmptyWhenTokenNotFound() {
        String token = "nonexistent-token";

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        Optional<RefreshToken> result = refreshTokenService.findByToken(token);

        assertTrue(result.isEmpty());
        verify(refreshTokenRepository).findByToken(token);
    }

    @Test
    void shouldFindByTokenSuccessfully() {
        String token = "valid-token";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);

        when(refreshTokenRepository.findByToken(token)).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> result = refreshTokenService.findByToken(token);

        assertTrue(result.isPresent());
        assertEquals(token, result.get().getToken());
        assertEquals(user, result.get().getUser());
        verify(refreshTokenRepository).findByToken(token);
    }

    @Test
    void shouldSaveTokenSuccessfully() {
        String token = "refresh-token";
        Claims claims = mock(Claims.class);

        // Stubs apenas neste teste
        when(claims.getId()).thenReturn("claim-id");
        when(claims.getExpiration()).thenReturn(Date.from(Instant.now().plusSeconds(3600)));

        refreshTokenService.saveToken(token, claims, user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());

        RefreshToken savedToken = captor.getValue();
        assertEquals(token, savedToken.getToken());
        assertEquals("claim-id", savedToken.getRefreshTokenId());
        assertEquals(user, savedToken.getUser());
        assertFalse(savedToken.isUsed());
        assertEquals(claims.getExpiration().toInstant(), savedToken.getExpiresAt());
    }

    @Test
    void shouldMarkTokenAsUsedSuccessfully() {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsed(false);

        refreshTokenService.markAsUsed(refreshToken);

        assertTrue(refreshToken.isUsed());
        verify(refreshTokenRepository).save(refreshToken);
    }
}
