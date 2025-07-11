package com.smartlist.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

public class JwtUtils {
    private static final String SECRET = "chave-secreta-beeeeeeem-longa-com-mais-de-64-caracteres-para-HS256!!";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    private static final long ACCESS_TOKEN_EXPIRATION = 15 * 60 * 1000; // 15 min
    private static final long REFRESH_TOKEN_EXPIRATION = 7 * 24 * 60 * 60 * 1000; // 7 dias

    public static String generateAccessToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim("sub", username)
                .claim("iat", new Date(now))
                .claim("exp", new Date(now + ACCESS_TOKEN_EXPIRATION))
                .signWith(KEY)
                .compact();
    }

    public static String generateRefreshToken(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .claim("sub", username)
                .claim("iat", new Date(now))
                .claim("exp", new Date(now + REFRESH_TOKEN_EXPIRATION))
                .claim("jid", UUID.randomUUID().toString())
                .signWith(KEY)
                .compact();
    }

    public static boolean isValidToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public static String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("sub", String.class);
        } catch (JwtException e) {
            return null;
        }
    }

    public static Date getExpirationFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.get("exp", Date.class);
        } catch (JwtException e) {
            return null;
        }
    }
}
