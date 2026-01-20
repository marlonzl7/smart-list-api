package com.smartlist.api.auth.service;

import com.smartlist.api.auth.dto.LoginRequest;
import com.smartlist.api.auth.dto.TokenResponse;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
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

    public TokenResponse login(LoginRequest dto, HttpServletResponse response) {
        User user = authenticate(dto);

        String accessToken = createAccessToken(dto.email());
        String refreshToken = createRefreshToken(dto.email());

        saveRefreshToken(refreshToken, user);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new TokenResponse(accessToken);
    }

    private User authenticate(LoginRequest dto) {
        log.info("Tentativa de autenticação iniciada. Email={}", dto.email());

        User user = userRepository.findByEmail(dto.email()).orElseThrow(() -> {
            log.warn("Falha de autenticação: usuário inexistente. Email={}", dto.email());
            return new InvalidCredentialsException("A1001", "Credenciais inválidas");
        });

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            log.warn("Falha de autenticação: senha incorreta. Email={}", dto.email());
            throw new InvalidCredentialsException("A1001", "Credenciais inválidas");
        }

        log.info("Login realizado com sucesso. UserId={}", user.getUserId());
        return user;
    }

    private String createAccessToken(String username) {
        return jwtUtils.generateAccessToken(username);
    }

    private String createRefreshToken(String username) {
        return jwtUtils.generateRefreshToken(username);
    }

    public void saveRefreshToken(String token, User user) {
        log.info("Refresh Token criado. UserId={}", user.getUserId());
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
            log.warn("Refresh token ausente ou inválido");
            throw new InvalidJwtException("A1002", "Refresh token inválido ou ausente");
        }

        Claims claims = jwtUtils.getClaimsFromToken(refreshToken);
        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByToken(refreshToken);

        if (optionalToken.isEmpty()) {
            log.warn("Refresh token não encontrado no banco");
            throw new InvalidJwtException("A1002", "Refresh token inválido");
        }

        if (optionalToken.get().isUsed()) {
            log.error(
                    "Reutilização de refresh token detectada. UserId={}",
                    optionalToken.get().getUser().getUserId()
            );
            refreshTokenRepository.invalidateAllByUser(optionalToken.get().getUser());
            throw new InvalidJwtException("A1003", "Refresh token reutilizado. Sessão invalidada.");
        }

        RefreshToken oldToken = optionalToken.get();
        oldToken.setUsed(true);
        refreshTokenRepository.save(oldToken);

        User user = oldToken.getUser();
        String newAccessToken = jwtUtils.generateAccessToken(user.getEmail());
        String newRefreshToken = jwtUtils.generateRefreshToken(user.getEmail());

        saveRefreshToken(newRefreshToken, user);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefreshToken)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        log.info("Refresh token rotacionado com sucesso. UserId={}", user.getUserId());
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

        log.info("Logout realizado. Refresh token invalidado se existente.");
    }
}
