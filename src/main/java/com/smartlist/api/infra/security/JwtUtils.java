package com.smartlist.api.infra.security;

import com.smartlist.api.infra.config.JwtProperties;
import com.smartlist.api.exceptions.InvalidJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
public class JwtUtils {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtils(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
        this.accessTokenExpiration = jwtProperties.getAccessTokenExpiration();
        this.refreshTokenExpiration = jwtProperties.getRefreshTokenExpiration();
    }

    public String generateAccessToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim("sub", username)
                .claim("iat", new Date(now))
                .claim("exp", new Date(now + accessTokenExpiration))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim("sub", username)
                .claim("iat", new Date(now))
                .claim("exp", new Date(now + refreshTokenExpiration))
                .claim("jti", UUID.randomUUID().toString())
                .signWith(key)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.warn("Token JWT inválido ou expirado: {}", e.getMessage());
            throw new InvalidJwtException("J4001", "Token JWT inválido ou expirado");
        }
    }

    public boolean isValidToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            return getClaimsFromToken(token).get("sub", String.class);
        } catch (JwtException e) {
            return null;
        }
    }

    public Date getExpirationFromToken(String token) {
        try {
            return getClaimsFromToken(token).get("exp", Date.class);
        } catch (JwtException e) {
            return null;
        }
    }
}
