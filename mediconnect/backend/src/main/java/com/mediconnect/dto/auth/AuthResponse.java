package com.mediconnect.dto.auth;

public record AuthResponse(
        String token,
        String refreshToken,
        Long id,
        String email,
        String role
) {
}
