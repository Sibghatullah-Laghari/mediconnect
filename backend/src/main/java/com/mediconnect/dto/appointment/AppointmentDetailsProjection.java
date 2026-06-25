package com.mediconnect.dto.appointment;

import com.mediconnect.model.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public interface AppointmentDetailsProjection {
    Long getId();
    AppointmentStatus getStatus();
    String getReason();
    LocalDate getAppointmentDate();
    LocalTime getAppointmentTime();
    Long getPatientId();
    String getPatientName();
    Long getDoctorId();
    String getDoctorName();
    String getPatientEmail();
    String getDoctorEmail();
}
