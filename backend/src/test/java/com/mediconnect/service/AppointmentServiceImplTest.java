package com.mediconnect.service;

import com.mediconnect.dto.appointment.CreateAppointmentRequest;
import com.mediconnect.exception.AppointmentConflictException;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.AppointmentStatus;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.security.OwnershipValidator;
import com.mediconnect.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceImplTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private SecurityUtils securityUtils;
    @Mock private OwnershipValidator ownershipValidator;

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-06-10T10:00:00Z"), ZoneId.of("UTC"));

    private AppointmentServiceImpl appointmentService;

    private Patient patient;
    private Doctor doctor;
    private User user;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentServiceImpl(
                appointmentRepository,
                patientRepository,
                doctorRepository,
                fixedClock,
                securityUtils,
                ownershipValidator
        );

        patient = new Patient();
        patient.setId(1L);
        patient.setUserId(10L);

        doctor = new Doctor();
        doctor.setId(2L);
        doctor.setName("Dr. Smith");

        user = new User();
        user.setId(10L);
        user.setRole(Role.PATIENT);
    }

    @Test
    void createAppointment_rejectsPastSlot() {
        var request = new CreateAppointmentRequest(
                1L, 2L, "Checkup",
                LocalDate.of(2026, 6, 9),
                LocalTime.of(9, 0)
        );

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("future");
    }

    @Test
    void createAppointment_rejectsConflictingSlot() {
        var request = new CreateAppointmentRequest(
                1L, 2L, "Checkup",
                LocalDate.of(2026, 6, 15),
                LocalTime.of(14, 0)
        );

        Appointment existing = new Appointment();
        existing.setStatus(AppointmentStatus.CONFIRMED);
        existing.setAppointmentDate(LocalDate.of(2026, 6, 15));
        existing.setAppointmentTime(LocalTime.of(14, 0));

        when(appointmentRepository.findByDoctorId(2L)).thenReturn(List.of(existing));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(AppointmentConflictException.class);
    }
}
