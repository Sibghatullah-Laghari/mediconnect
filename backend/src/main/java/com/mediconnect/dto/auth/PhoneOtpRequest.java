package com.mediconnect.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PhoneOtpRequest(
        @Email @NotBlank String email,
        @NotBlank @Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone
) {}