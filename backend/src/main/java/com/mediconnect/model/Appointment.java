package com.mediconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @Setter
    @Column(nullable = false, length = 500)
    private String reason;

    @Setter
    @Column(nullable = false)
    private LocalDate appointmentDate;

    @Setter
    @Column(nullable = false)
    private LocalTime appointmentTime;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
}
