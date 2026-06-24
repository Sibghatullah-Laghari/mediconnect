package com.mediconnect.dto.doctor;

import com.mediconnect.model.Gender;

import java.math.BigDecimal;

/**
 * Response DTO containing doctor information returned to clients.
 *
 * @param id             the unique identifier of the doctor
 * @param name           the doctor's full name
 * @param gender         the doctor's gender
 * @param specialization the doctor's medical specialization
 * @param phone          the doctor's contact phone number
 * @param email          the doctor's email address
 * @param fee            the consultation fee charged by the doctor
 * @param experience     the doctor's years of professional experience
 */
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