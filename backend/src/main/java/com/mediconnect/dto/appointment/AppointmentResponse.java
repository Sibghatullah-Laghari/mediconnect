package com.mediconnect.dto.appointment;

import com.mediconnect.model.Appointment;
import com.mediconnect.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor

public class AppointmentResponse {

    private Long id;
    private AppointmentStatus status;

    private String reason;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private Long patientId;
    private Long doctorId;
    private String patientName;
    private String doctorName;

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .status(appointment.getStatus())
                .build();
    }
}
