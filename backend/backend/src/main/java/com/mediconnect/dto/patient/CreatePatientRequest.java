package com.mediconnect.dto.patient;

import com.mediconnect.model.Gender;
import jakarta.validation.constraints.*;

public record CreatePatientRequest(

        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100,
                message = "Name must be between 2 and 100 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^[0-9]{10,15}$",
                message = "Phone must be 10 to 15 digits only"
        )
        String phone,

        @NotNull(message = "Age is required")
        @Min(value = 1, message = "Age must be at least 1")
        @Max(value = 120, message = "Age cannot exceed 120")
        Integer age,

        @NotNull(message = "Gender is required")
        Gender gender,

        String address

) {}