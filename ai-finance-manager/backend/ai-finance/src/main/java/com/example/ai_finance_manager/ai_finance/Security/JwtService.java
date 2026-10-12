package com.example.ai_finance_manager.ai_finance.Security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.access-minutes}") long accessMinutes,
                      @Value("${app.jwt.refresh-days}") long refreshDays) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTtl = Duration.ofMinutes(accessMinutes);
        this.refreshTtl = Duration.ofDays(refreshDays);
    }

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(userDetails, TYPE_ACCESS, accessTtl);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(userDetails, TYPE_REFRESH, refreshTtl);
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        return isValid(token, userDetails, TYPE_ACCESS);
    }

    public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        return isValid(token, userDetails, TYPE_REFRESH);
    }

    private boolean isValid(String token, UserDetails userDetails, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            return userDetails.getUsername().equals(claims.getSubject())
                    && expectedType.equals(claims.get(CLAIM_TYPE, String.class));
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    private String buildToken(UserDetails userDetails, String type, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .claim(CLAIM_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
