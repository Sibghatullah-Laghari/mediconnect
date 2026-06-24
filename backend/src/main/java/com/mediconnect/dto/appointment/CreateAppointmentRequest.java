package com.mediconnect.dto.appointment;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Request DTO used to create a new appointment.
 *
 * @param patientId       the unique identifier of the patient booking the appointment
 * @param doctorId        the unique identifier of the doctor with whom the appointment is scheduled
 * @param reason          the reason or purpose of the appointment
 * @param appointmentDate the scheduled appointment date; must be today or a future date
 * @param appointmentTime the scheduled appointment time
 */
public record CreateAppointmentRequest(

        @NotNull
        Long patientId,

        @NotNull
        Long doctorId,

        @NotBlank
        @Size(max = 500)
        String reason,

        @NotNull
        @FutureOrPresent
        LocalDate appointmentDate,

        @NotNull
        LocalTime appointmentTime

) {
}