package com.mediconnect.dto.auth;

public record AuthResponse(
        String token,
        UserResponse user
) {
}

