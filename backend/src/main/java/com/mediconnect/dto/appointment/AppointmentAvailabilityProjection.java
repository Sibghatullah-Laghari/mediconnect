package com.mediconnect.dto.appointment;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AppointmentAvailabilityProjection {
    Long getId();
    LocalDate getAppointmentDate();
    LocalTime getAppointmentTime();
}
