package com.mediconnect.dto.appointment;

import com.mediconnect.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO used to update the status of an appointment.
 *
 * @param status the new appointment status
 */
public record StatusRequest(

        @NotNull
        AppointmentStatus status

) {
}