package com.smartlist.api.auth.service;

import com.smartlist.api.auth.dto.LoginDTO;
import com.smartlist.api.exceptions.InvalidCredentialsException;
import com.smartlist.api.exceptions.InvalidJwtException;
import com.smartlist.api.refreshtoken.model.RefreshToken;
import com.smartlist.api.refreshtoken.repository.RefreshTokenRepository;
import com.smartlist.api.security.JwtUtils;
import com.smartlist.api.user.model.User;
import com.smartlist.api.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public boolean authenticate(LoginDTO dto) {
        User user = userRepository.findByEmail(dto.email()).orElseThrow(() -> new InvalidCredentialsException("008", "Credenciais inválidas"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new InvalidCredentialsException("008", "Credenciais inválidas");
        }

        return true;
    }

    public String createAccessToken(String username) {
        return jwtUtils.generateAccessToken(username);
    }

    public String createRefreshToken(String username) {
        return jwtUtils.generateRefreshToken(username);
    }

    public void saveRefreshToken(String token, User user) {
        Claims claims = jwtUtils.getClaimsFromToken(token);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshTokenId(claims.getId());
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(claims.getExpiration().toInstant());
        refreshToken.setUsed(false);

        refreshTokenRepository.save(refreshToken);
    }

    public String refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null || !jwtUtils.isValidToken(refreshToken)) {
            throw new InvalidJwtException("009", "Refresh token inválido ou ausente");
        }

        Claims claims = jwtUtils.getClaimsFromToken(refreshToken);
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(refreshToken);

        if (optionalToken.isEmpty() || optionalToken.get().isUsed()) {
            throw new InvalidJwtException("010", "Refresh token já utilizado ou inexistente");
        }

        RefreshToken oldToken = optionalToken.get();
        oldToken.setUsed(true);
        refreshTokenRepository.save(oldToken);

        User user = oldToken.getUser();
        String newAccessToken = jwtUtils.generateAccessToken(user.getEmail());
        String newRefreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        saveRefreshToken(newRefreshToken, user);

        String cookieHeader = String.format("refreshToken=%s; HttpOnly; Path=/; Max-Age=%d; SameSite=Lax",
                newRefreshToken, 7 * 24 * 60 * 60);
        response.setHeader("Set-Cookie", cookieHeader);

        return newAccessToken;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken != null && jwtUtils.isValidToken(refreshToken)) {
            Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(refreshToken);
            optionalToken.ifPresent(token -> {
                token.setUsed(true);
                refreshTokenRepository.save(token);
            });
        }

        String clearCookie = "refreshToken=; HttpOnly; Path=/; Max-Age=0; SameSite=Lax";
        response.setHeader("Set-Cookie", clearCookie);
    }
}
