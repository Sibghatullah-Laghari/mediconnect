package com.mediconnect.dto.appointment;

import com.mediconnect.model.Appointment;
import com.mediconnect.model.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Response DTO representing appointment details returned to clients.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    /**
     * Converts an Appointment entity into an AppointmentResponse DTO.
     *
     * @param appointment the appointment entity to convert
     * @return mapped appointment response
     */
    public static AppointmentResponse fromEntity(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .status(appointment.getStatus())
                .reason(appointment.getReason())
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .patientId(appointment.getPatient().getId())
                .doctorId(appointment.getDoctor().getId())
                .patientName(appointment.getPatient().getName())
                .doctorName(appointment.getDoctor().getName())
                .build();
    }
}
