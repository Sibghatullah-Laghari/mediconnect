package com.mediconnect.dto.auth;

import com.mediconnect.model.Role;

public record UserResponse(
        Long id,
        String email,
        Role role
) {
}

