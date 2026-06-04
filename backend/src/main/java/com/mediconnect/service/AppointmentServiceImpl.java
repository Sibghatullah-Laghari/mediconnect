package com.mediconnect.service;

import com.mediconnect.dto.appointment.AppointmentResponse;
import com.mediconnect.dto.appointment.CreateAppointmentRequest;
import com.mediconnect.exception.AppointmentConflictException;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.InvalidStatusTransitionException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.AppointmentStatus;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Patient;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final Clock clock;

    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        validateBookableAppointmentSlot(request);

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        Appointment appointment = new Appointment();
        appointment.setReason(request.reason());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setAppointmentTime(request.appointmentTime());
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);

        Appointment saved = appointmentRepository.save(appointment);
        return toResponse(saved);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getStatus(),
                appointment.getReason(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getPatient().getId(),
                appointment.getDoctor().getId(),
                appointment.getPatient().getName(),
                appointment.getDoctor().getName()
        );
    }

    @Override
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
        return toResponse(appointment);
    }

    private Appointment getAppointmentEntityById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    public void validateAppointmentDate(CreateAppointmentRequest request) {
        validateBookableAppointmentSlot(request);
    }

    public boolean canBookAppointmentSlot(LocalDate appointmentDate, LocalTime appointmentTime) {
        if (appointmentDate == null || appointmentTime == null) {
            return false;
        }

        LocalDateTime requestedSlot = LocalDateTime.of(appointmentDate, appointmentTime);
        return requestedSlot.isAfter(LocalDateTime.now(clock));
    }

    public void validateBookableAppointmentSlot(CreateAppointmentRequest request) {
        if (request == null) {
            throw new BadRequestException("Appointment request is required");
        }

        // First, ensure the requested date/time is in the future
        if (!canBookAppointmentSlot(request.appointmentDate(), request.appointmentTime())) {
            throw new BadRequestException("Appointment must be scheduled in the future");
        }

        // Then, ensure the doctor's slot is not already taken. The repository does not
        // expose a direct existsByDoctorIdAndAppointmentDateAndAppointmentTime method,
        // so fetch the doctor's appointments and check for a matching date/time.
        java.util.List<Appointment> doctorAppointments = appointmentRepository.findByDoctorId(request.doctorId());
        boolean slotTaken = doctorAppointments.stream().anyMatch(a ->
                request.appointmentDate().equals(a.getAppointmentDate()) &&
                        request.appointmentTime().equals(a.getAppointmentTime())
        );

        if (slotTaken) {
            Doctor doctor = doctorRepository.findById(request.doctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

            throw new AppointmentConflictException(
                    "Dr. " + doctor.getName() +
                            " is already booked on " + request.appointmentDate() +
                            " at " + request.appointmentTime()
            );
        }
    }

    private boolean isDoctorSlotTaken(Long doctorId, LocalDate appointmentDate, LocalTime appointmentTime) {
        if (doctorId == null || appointmentDate == null || appointmentTime == null) {
            return false;
        }

        java.util.List<Appointment> doctorAppointments = appointmentRepository.findByDoctorId(doctorId);
        return doctorAppointments.stream().anyMatch(a ->
                appointmentDate.equals(a.getAppointmentDate()) && appointmentTime.equals(a.getAppointmentTime())
        );
    }

    public void validateBookableAppointmentSlot(LocalDate appointmentDate, LocalTime appointmentTime) {
        if (!canBookAppointmentSlot(appointmentDate, appointmentTime)) {
            throw new BadRequestException("Appointment must be scheduled in the future");
        }

    }

    // In AppointmentServiceImpl:
    @Override
    public AppointmentResponse updateStatus(Long id, AppointmentStatus newStatus) {

        Appointment appointment = getAppointmentEntityById(id);

        AppointmentStatus current = appointment.getStatus();

        // Check valid transitions
        boolean isValidTransition =
                (current == AppointmentStatus.PENDING &&
                        (newStatus == AppointmentStatus.CONFIRMED ||
                                newStatus == AppointmentStatus.CANCELLED))
                        ||
                        (current == AppointmentStatus.CONFIRMED &&
                                (newStatus == AppointmentStatus.COMPLETED ||
                                        newStatus == AppointmentStatus.CANCELLED));

        if (!isValidTransition) {
            throw new InvalidStatusTransitionException(current, newStatus);
        }

        appointment.setStatus(newStatus);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentEntityById(id);
        appointmentRepository.delete(appointment);
    }
}