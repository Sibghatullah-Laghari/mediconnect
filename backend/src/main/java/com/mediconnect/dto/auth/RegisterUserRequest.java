package com.mediconnect.dto.auth;

import com.mediconnect.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUserRequest(
        @NotBlank @Size(max = 100) String name,
        @Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 255) String password,
        @NotNull Role role
) {
}
