package com.smartlist.api.refreshtoken.service;

import com.smartlist.api.refreshtoken.model.RefreshToken;
import com.smartlist.api.refreshtoken.repository.RefreshTokenRepository;
import com.smartlist.api.user.model.User;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public void saveToken(String token, Claims claims, User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshTokenId(claims.getId());
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setCreatedAt(claims.getIssuedAt().toInstant());
        refreshToken.setUpdatedAt(claims.getExpiration().toInstant());
        refreshToken.setUsed(false);

        refreshTokenRepository.save(refreshToken);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public void markAsUsed(RefreshToken token) {
        token.setUsed(true);
        refreshTokenRepository.save(token);
    }
}
