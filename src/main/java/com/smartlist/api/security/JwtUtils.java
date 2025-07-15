package com.smartlist.api.security;

import com.smartlist.api.exceptions.InvalidJwtException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtUtils {

    private static final String SECRET = "chave-secreta-beeeeeeem-longa-com-mais-de-64-caracteres-para-HS256!!";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000; // 15 min
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 dias

    public String generateAccessToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim("sub", username)
                .claim("iat", new Date(now))
                .claim("exp", new Date(now + ACCESS_TOKEN_EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    public String generateRefreshToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim("sub", username)
                .claim("iat", new Date(now))
                .claim("exp", new Date(now + REFRESH_TOKEN_EXPIRATION))
                .claim("jti", UUID.randomUUID().toString())
                .signWith(KEY)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            throw new InvalidJwtException("007", "Token JWT inválido ou expirado");
        }
    }

    public boolean isValidToken(String token) {
        try {
            getClaimsFromToken(token);
            return true;
        } catch (JwtException e) {
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
