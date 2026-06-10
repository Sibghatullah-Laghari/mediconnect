package com.mediconnect.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private final JwtTokenProvider provider = new JwtTokenProvider();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(provider, "jwtSecret",
                "test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm");
        ReflectionTestUtils.setField(provider, "accessTokenExpirationMs", 900000L);
    }

    @Test
    void generatesAndValidatesAccessToken() {
        String token = provider.generateAccessToken("user@example.com", "PATIENT");

        assertThat(provider.isTokenValid(token)).isTrue();
        assertThat(provider.extractEmail(token)).isEqualTo("user@example.com");
    }

    @Test
    void rejectsTamperedToken() {
        String token = provider.generateAccessToken("user@example.com", "PATIENT");
        assertThat(provider.isTokenValid(token + "tampered")).isFalse();
    }
}
