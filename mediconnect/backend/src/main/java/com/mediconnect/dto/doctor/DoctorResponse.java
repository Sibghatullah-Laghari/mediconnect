package com.mediconnect.dto.doctor;

import com.mediconnect.model.Gender;

import java.math.BigDecimal;

public record DoctorResponse(
        Long id,
        String name,
        Gender gender,
        String specialization,
        String phone,
        String email,
        BigDecimal fee,
        Integer experience
) {
}

