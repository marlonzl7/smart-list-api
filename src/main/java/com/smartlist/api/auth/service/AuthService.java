package com.smartlist.api.auth.service;

import com.smartlist.api.auth.dto.LoginDTO;
import com.smartlist.api.exceptions.InvalidCredentialsException;
import com.smartlist.api.exceptions.InvalidJwtException;
import com.smartlist.api.refreshtoken.model.RefreshToken;
import com.smartlist.api.refreshtoken.repository.RefreshTokenRepository;
import com.smartlist.api.infra.security.JwtUtils;
import com.smartlist.api.user.model.User;
import com.smartlist.api.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Slf4j
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
        log.info("Iniciada tentativa de autenticação para email: {}", dto.email());

        User user = userRepository.findByEmail(dto.email()).orElseThrow(() -> {
            log.error("Usuário não encontrado com email: {}", dto.email());
            return new InvalidCredentialsException("A1001", "Credenciais inválidas");
        });

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            log.error("Senha incorreta para email: {}", dto.email());
            throw new InvalidCredentialsException("A1001", "Credenciais inválidas");
        }

        log.info("Autenticação efetuada com sucesso para mail: {}", user.getEmail());

        return true;
    }

    public String createAccessToken(String username) {
        return jwtUtils.generateAccessToken(username);
    }

    public String createRefreshToken(String username) {
        return jwtUtils.generateRefreshToken(username);
    }

    public void saveRefreshToken(String token, User user) {
        log.info("Refresh Token criado para usuário ID: {}", user.getUserId());
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
            log.error("Refresh token inválido ou ausente");
            throw new InvalidJwtException("A1002", "Refresh token inválido ou ausente");
        }

        Claims claims = jwtUtils.getClaimsFromToken(refreshToken);
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(refreshToken);

        if (optionalToken.isEmpty() || optionalToken.get().isUsed()) {
            log.error("Refresh token já utilizado ou inexistente");
            throw new InvalidJwtException("A1003", "Refresh token já utilizado ou inexistente");
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

        log.info("Refresh token rotacionado com sucesso para usuário ID: {}", user.getUserId());

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

        log.info("Logout efetuado. Refresh token invalidado (se presente).");
    }
}
