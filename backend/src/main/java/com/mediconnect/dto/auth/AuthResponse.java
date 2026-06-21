package com.mediconnect.dto.auth;

public record AuthResponse(
        String token,
        String refreshToken,
        UserResponse user
) {
}
