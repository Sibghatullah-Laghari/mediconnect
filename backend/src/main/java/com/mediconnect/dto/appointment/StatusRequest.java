package com.mediconnect.dto.appointment;

import com.mediconnect.model.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(
        @NotNull AppointmentStatus status
) {
}

