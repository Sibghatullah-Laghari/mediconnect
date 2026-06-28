package com.mediconnect.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PhoneOtpVerifyRequest(
        @Email @NotBlank String email,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone,
        @NotBlank @Size(min = 6, max = 6) String otp
) {}