package com.mediconnect.dto.patient;

import com.mediconnect.model.Gender;

import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String name,
        String email,
        String phone,
        LocalDate dateOfBirth,
        Gender gender,
        String address
) {
}

