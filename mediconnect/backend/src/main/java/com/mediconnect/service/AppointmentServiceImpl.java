package com.mediconnect.service;

import com.mediconnect.dto.appointment.AppointmentResponse;
import com.mediconnect.dto.appointment.CreateAppointmentRequest;
import com.mediconnect.exception.AppointmentConflictException;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.InvalidStatusTransitionException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.AppointmentStatus;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Role;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final Clock clock;

    @Override
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        validateBookableAppointmentSlot(request);

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.patientId()));
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.doctorId()));
        ensureCanCreateAppointment(patient);

        Appointment appointment = new Appointment();
        appointment.setReason(request.reason());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setAppointmentTime(request.appointmentTime());
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);

        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = getAppointmentEntityById(id);
        ensureCanAccessAppointment(appointment);
        return toResponse(appointment);
    }

    @Override
    public List<AppointmentResponse> getAllAppointments() {
        List<Appointment> appointments;
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            appointments = appointmentRepository.findAll();
        } else if (SecurityUtils.hasRole(Role.PATIENT)) {
            appointments = appointmentRepository.findByPatientEmail(SecurityUtils.getCurrentUserEmail());
        } else if (SecurityUtils.hasRole(Role.DOCTOR)) {
            appointments = appointmentRepository.findByDoctorEmail(SecurityUtils.getCurrentUserEmail());
        } else {
            throw new UnauthorizedException("You do not have permission to view appointments");
        }

        return appointments.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getAppointmentsByPatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
        ensureCanAccessPatientAppointments(patient);

        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
        ensureCanAccessDoctorAppointments(doctor);

        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponse updateAppointment(Long id, CreateAppointmentRequest request) {
        validateBookableAppointmentSlot(request);

        Appointment appointment = getAppointmentEntityById(id);
        ensureCanModifyAppointment(appointment);

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.patientId()));
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.doctorId()));
        ensureCanCreateAppointment(patient);

        if (!appointment.getDoctor().getId().equals(doctor.getId())
                || !appointment.getAppointmentDate().equals(request.appointmentDate())
                || !appointment.getAppointmentTime().equals(request.appointmentTime())) {
            validateDoctorAvailability(doctor.getId(), request.appointmentDate(), request.appointmentTime(), appointment.getId());
        }

        appointment.setReason(request.reason());
        appointment.setAppointmentDate(request.appointmentDate());
        appointment.setAppointmentTime(request.appointmentTime());
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);

        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentResponse updateStatus(Long id, AppointmentStatus newStatus) {
        Appointment appointment = getAppointmentEntityById(id);
        ensureCanManageStatus(appointment);

        AppointmentStatus current = appointment.getStatus();
        boolean isValidTransition =
                (current == AppointmentStatus.PENDING
                        && (newStatus == AppointmentStatus.CONFIRMED || newStatus == AppointmentStatus.CANCELLED))
                        || (current == AppointmentStatus.CONFIRMED
                        && (newStatus == AppointmentStatus.COMPLETED
                        || newStatus == AppointmentStatus.CANCELLED
                        || newStatus == AppointmentStatus.NO_SHOW));

        if (!isValidTransition) {
            throw new InvalidStatusTransitionException(current, newStatus);
        }

        appointment.setStatus(newStatus);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentResponse confirmAppointment(Long id) {
        return updateStatus(id, AppointmentStatus.CONFIRMED);
    }

    @Override
    public AppointmentResponse completeAppointment(Long id) {
        return updateStatus(id, AppointmentStatus.COMPLETED);
    }

    @Override
    public AppointmentResponse cancelAppointment(Long id) {
        Appointment appointment = getAppointmentEntityById(id);
        ensureCanModifyAppointment(appointment);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentEntityById(id);
        ensureCanModifyAppointment(appointment);
        appointmentRepository.delete(appointment);
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

        if (!canBookAppointmentSlot(request.appointmentDate(), request.appointmentTime())) {
            throw new BadRequestException("Appointment must be scheduled in the future");
        }

        validateDoctorAvailability(request.doctorId(), request.appointmentDate(), request.appointmentTime(), null);
    }

    private Appointment getAppointmentEntityById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
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

    private void validateDoctorAvailability(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            Long excludedAppointmentId
    ) {
        boolean slotTaken = appointmentRepository.findByDoctorId(doctorId).stream().anyMatch(appointment ->
                (excludedAppointmentId == null || !appointment.getId().equals(excludedAppointmentId))
                        && appointmentDate.equals(appointment.getAppointmentDate())
                        && appointmentTime.equals(appointment.getAppointmentTime())
        );

        if (slotTaken) {
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
            throw new AppointmentConflictException(
                    "Dr. " + doctor.getName() + " is already booked on " + appointmentDate + " at " + appointmentTime
            );
        }
    }

    private void ensureCanCreateAppointment(Patient patient) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        if (SecurityUtils.hasRole(Role.PATIENT)
                && SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(patient.getEmail())) {
            return;
        }

        throw new UnauthorizedException("You can only create appointments for your own patient profile");
    }

    private void ensureCanAccessAppointment(Appointment appointment) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        String currentEmail = SecurityUtils.getCurrentUserEmail();
        if (SecurityUtils.hasRole(Role.PATIENT)
                && currentEmail.equalsIgnoreCase(appointment.getPatient().getEmail())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.DOCTOR)
                && currentEmail.equalsIgnoreCase(appointment.getDoctor().getEmail())) {
            return;
        }

        throw new UnauthorizedException("You do not have permission to access this appointment");
    }

    private void ensureCanManageStatus(Appointment appointment) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        if (SecurityUtils.hasRole(Role.DOCTOR)
                && SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(appointment.getDoctor().getEmail())) {
            return;
        }

        throw new UnauthorizedException("Only the assigned doctor or an admin can update appointment status");
    }

    private void ensureCanModifyAppointment(Appointment appointment) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        String currentEmail = SecurityUtils.getCurrentUserEmail();
        if (SecurityUtils.hasRole(Role.PATIENT)
                && currentEmail.equalsIgnoreCase(appointment.getPatient().getEmail())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.DOCTOR)
                && currentEmail.equalsIgnoreCase(appointment.getDoctor().getEmail())) {
            return;
        }

        throw new UnauthorizedException("You do not have permission to modify this appointment");
    }

    private void ensureCanAccessPatientAppointments(Patient patient) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        if (SecurityUtils.hasRole(Role.PATIENT)
                && SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(patient.getEmail())) {
            return;
        }

        throw new UnauthorizedException("You do not have permission to view these appointments");
    }

    private void ensureCanAccessDoctorAppointments(Doctor doctor) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        if (SecurityUtils.hasRole(Role.DOCTOR)
                && SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(doctor.getEmail())) {
            return;
        }

        throw new UnauthorizedException("You do not have permission to view these appointments");
    }
}
