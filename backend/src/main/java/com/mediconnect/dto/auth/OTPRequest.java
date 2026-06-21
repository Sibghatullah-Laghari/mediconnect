package com.mediconnect.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OTPRequest(
        @NotBlank @Email String email,
        @NotBlank String otp
) {
}
