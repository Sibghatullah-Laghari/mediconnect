package com.mediconnect.dto.appointment;

import com.mediconnect.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponse(
        Long id,
        AppointmentStatus status,
        String reason,
        LocalDate appointmentDate,
        LocalTime appointmentTime,
        Long patientId,
        Long doctorId
) {
}

