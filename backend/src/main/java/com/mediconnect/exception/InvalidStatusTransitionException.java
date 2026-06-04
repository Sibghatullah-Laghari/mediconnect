package com.mediconnect.exception;

import com.mediconnect.model.AppointmentStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(
            AppointmentStatus currentStatus,
            AppointmentStatus newStatus
    ) {
        super("Appointment status cannot be changed from " + currentStatus + " to " + newStatus);
    }
}
