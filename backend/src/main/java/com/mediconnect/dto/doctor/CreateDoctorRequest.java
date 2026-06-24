package com.mediconnect.dto.doctor;

import com.mediconnect.model.Gender;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO used to create a new doctor profile.
 *
 * @param name           the doctor's full name
 * @param gender         the doctor's gender
 * @param specialization the doctor's medical specialization
 * @param phone          the doctor's contact phone number
 * @param email          the doctor's email address
 * @param fee            the consultation fee charged by the doctor
 * @param experience     the doctor's years of professional experience
 */
public record CreateDoctorRequest(

        @NotBlank
        @Size(max = 100)
        String name,

        @NotNull
        Gender gender,

        @NotBlank
        @Size(max = 100)
        String specialization,

        @NotBlank
        @Size(max = 20)
        String phone,

        @Email
        @NotBlank
        @Size(max = 255)
        String email,

        @NotNull
        @DecimalMin("0.0")
        BigDecimal fee,

        @NotNull
        @PositiveOrZero
        Integer experience

) {
}