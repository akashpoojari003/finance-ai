package com.example.ai_finance_manager.ai_finance.security;

import com.example.ai_finance_manager.ai_finance.Security.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(
            "unit-test-secret-key-that-is-at-least-32-bytes".getBytes(StandardCharsets.UTF_8));
    private static final String OTHER_SECRET = Base64.getEncoder().encodeToString(
            "another-unit-test-secret-key-at-least-32-bytes".getBytes(StandardCharsets.UTF_8));

    private JwtService jwtService;
    private UserDetails user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, 15, 7);
        user = User.withUsername("jane@example.com").password("hash").roles("USER").build();
    }

    @Test
    void accessToken_containsUsernameAndIsValid() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.extractUsername(token)).isEqualTo("jane@example.com");
        assertThat(jwtService.isAccessTokenValid(token, user)).isTrue();
    }

    @Test
    void refreshToken_isValidOnlyAsRefreshToken() {
        String token = jwtService.generateRefreshToken(user);

        assertThat(jwtService.isRefreshTokenValid(token, user)).isTrue();
        assertThat(jwtService.isAccessTokenValid(token, user)).isFalse();
    }

    @Test
    void accessToken_isNotValidAsRefreshToken() {
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isRefreshTokenValid(token, user)).isFalse();
    }

    @Test
    void token_isInvalidForDifferentUser() {
        UserDetails other = User.withUsername("other@example.com").password("hash").roles("USER").build();
        String token = jwtService.generateAccessToken(user);

        assertThat(jwtService.isAccessTokenValid(token, other)).isFalse();
    }

    @Test
    void expiredToken_isInvalidAndRejectedOnParse() {
        JwtService expiring = new JwtService(SECRET, -1, -1);
        String token = expiring.generateAccessToken(user);

        assertThat(jwtService.isAccessTokenValid(token, user)).isFalse();
        assertThrows(ExpiredJwtException.class, () -> jwtService.extractUsername(token));
    }

    @Test
    void tokenSignedWithDifferentKey_isInvalid() {
        String token = new JwtService(OTHER_SECRET, 15, 7).generateAccessToken(user);

        assertThat(jwtService.isAccessTokenValid(token, user)).isFalse();
        assertThrows(JwtException.class, () -> jwtService.extractUsername(token));
    }

    @Test
    void tamperedPayload_isInvalid() {
        String[] parts = jwtService.generateAccessToken(user).split("\\.");
        String forgedPayload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString("{\"sub\":\"hacker@example.com\"}".getBytes(StandardCharsets.UTF_8));
        String forged = parts[0] + "." + forgedPayload + "." + parts[2];

        assertThat(jwtService.isAccessTokenValid(forged, user)).isFalse();
        assertThrows(JwtException.class, () -> jwtService.extractUsername(forged));
    }

    @Test
    void garbageToken_isInvalid() {
        assertThat(jwtService.isAccessTokenValid("not-a-jwt", user)).isFalse();
    }

    @Test
    void tokensGeneratedBackToBack_areUnique() {
        assertThat(jwtService.generateAccessToken(user))
                .isNotEqualTo(jwtService.generateAccessToken(user));
    }
}