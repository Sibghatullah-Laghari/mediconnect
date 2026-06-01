package com.mediconnect.dto.doctor;

import com.mediconnect.model.Gender;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateDoctorRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull Gender gender,
        @NotBlank @Size(max = 100) String specialization,
        @NotBlank @Size(max = 20) String phone,
        @Email @NotBlank @Size(max = 255) String email,
        @NotNull @DecimalMin("0.0") BigDecimal fee,
        @NotNull @PositiveOrZero Integer experience
) {
}

