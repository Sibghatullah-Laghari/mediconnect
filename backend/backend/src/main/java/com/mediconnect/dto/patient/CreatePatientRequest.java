package com.mediconnect.dto.patient;

import com.mediconnect.model.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreatePatientRequest(
        @NotBlank @Size(max = 100) String name,
        @Email @NotBlank @Size(max = 255) String email,
        @NotBlank @Size(max = 20) String phone,
        @NotNull @Past LocalDate dateOfBirth,
        @NotNull Gender gender,
        @Size(max = 500) String address
) {
}

