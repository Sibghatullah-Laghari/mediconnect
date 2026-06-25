package com.mediconnect.service;

import com.mediconnect.dto.appointment.AppointmentAvailabilityProjection;
import com.mediconnect.dto.appointment.AppointmentDetailsProjection;
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
import com.mediconnect.model.User;
import com.mediconnect.repository.AppointmentRepository;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final UserRepository userRepository;

    @Override
    public AppointmentResponse createAppointment(CreateAppointmentRequest request) {
        validateBookableAppointmentSlot(request, null);
        ensureEmailVerified();

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
    public AppointmentResponse updateAppointment(Long id, CreateAppointmentRequest request) {
        Appointment appointment = getAppointmentEntityById(id);
        ensureCanModifyAppointment(appointment);
        ensureEmailVerified();

        validateBookableAppointmentSlot(request, appointment.getId());

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient", request.patientId()));
        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", request.doctorId()));
        ensureCanCreateAppointment(patient);

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
    public Page<AppointmentResponse> getAllAppointments(Pageable pageable) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return appointmentRepository.findAllWithDetails(pageable).map(this::toResponse);
        } else if (SecurityUtils.hasRole(Role.PATIENT)) {
            return appointmentRepository.findByPatientEmail(SecurityUtils.getCurrentUserEmail(), pageable)
                    .map(this::toResponse);
        } else if (SecurityUtils.hasRole(Role.DOCTOR)) {
            return appointmentRepository.findByDoctorEmail(SecurityUtils.getCurrentUserEmail(), pageable)
                    .map(this::toResponse);
        }

        throw new UnauthorizedException("You do not have permission to view appointments");
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
    public Page<AppointmentResponse> getAppointmentsByPatient(Long patientId, Pageable pageable) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
        ensureCanAccessPatientAppointments(patient);

        return appointmentRepository.findByPatientId(patientId, pageable).map(this::toResponse);
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
    public Page<AppointmentResponse> getAppointmentsByDoctor(Long doctorId, Pageable pageable) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
        ensureCanAccessDoctorAppointments(doctor);

        return appointmentRepository.findByDoctorId(doctorId, pageable).map(this::toResponse);
    }

    @Override
    public AppointmentResponse updateStatus(Long id, AppointmentStatus newStatus) {
        Appointment appointment = getAppointmentEntityById(id);
        ensureCanManageStatus(appointment, newStatus);

        AppointmentStatus current = appointment.getStatus();
        boolean isValidTransition =
                (current == AppointmentStatus.PENDING
                        && (newStatus == AppointmentStatus.CONFIRMED || newStatus == AppointmentStatus.CANCELLED))
                        || (current == AppointmentStatus.CONFIRMED
                        && (newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.CANCELLED));

        if (!isValidTransition) {
            throw new InvalidStatusTransitionException(current, newStatus);
        }

        appointment.setStatus(newStatus);
        return toResponse(appointmentRepository.save(appointment));
    }

    @Override
    public AppointmentResponse confirmAppointment(Long id) {
        requireDoctorOrAdmin();
        return updateStatus(id, AppointmentStatus.CONFIRMED);
    }

    @Override
    public AppointmentResponse completeAppointment(Long id) {
        requireDoctorOrAdmin();
        return updateStatus(id, AppointmentStatus.COMPLETED);
    }

    @Override
    public AppointmentResponse cancelAppointment(Long id) {
        Appointment appointment = getAppointmentEntityById(id);
        ensureCanCancelAppointment(appointment);
        return updateStatus(id, AppointmentStatus.CANCELLED);
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

    public void validateBookableAppointmentSlot(CreateAppointmentRequest request, Long excludedAppointmentId) {
        if (request == null) {
            throw new BadRequestException("Appointment request is required");
        }

        if (!canBookAppointmentSlot(request.appointmentDate(), request.appointmentTime())) {
            throw new BadRequestException("Appointment must be scheduled in the future");
        }

        validateDoctorAvailability(request.doctorId(), request.appointmentDate(), request.appointmentTime(), excludedAppointmentId);
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

    private void ensureCanManageStatus(Appointment appointment, AppointmentStatus newStatus) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        String currentEmail = SecurityUtils.getCurrentUserEmail();
        boolean isDoctor = SecurityUtils.hasRole(Role.DOCTOR)
                && currentEmail.equalsIgnoreCase(appointment.getDoctor().getEmail());
        boolean isPatient = SecurityUtils.hasRole(Role.PATIENT)
                && currentEmail.equalsIgnoreCase(appointment.getPatient().getEmail());

        if (newStatus == AppointmentStatus.CONFIRMED || newStatus == AppointmentStatus.COMPLETED) {
            if (isDoctor) {
                return;
            }
            throw new UnauthorizedException("Only the assigned doctor or an admin can confirm or complete appointments");
        }

        if (newStatus == AppointmentStatus.CANCELLED) {
            if (isDoctor || isPatient) {
                return;
            }
            throw new UnauthorizedException("Only the assigned doctor, the patient owner, or an admin can cancel appointments");
        }

        if (isDoctor || isPatient) {
            return;
        }

        throw new UnauthorizedException("You do not have permission to update appointment status");
    }

    private void ensureCanCancelAppointment(Appointment appointment) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        String currentEmail = SecurityUtils.getCurrentUserEmail();
        if (SecurityUtils.hasRole(Role.DOCTOR)
                && currentEmail.equalsIgnoreCase(appointment.getDoctor().getEmail())) {
            return;
        }

        if (SecurityUtils.hasRole(Role.PATIENT)
                && currentEmail.equalsIgnoreCase(appointment.getPatient().getEmail())) {
            return;
        }

        throw new UnauthorizedException("Only the assigned doctor, the patient owner, or an admin can cancel appointments");
    }

    private void requireDoctorOrAdmin() {
        if (SecurityUtils.hasRole(Role.DOCTOR) || SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }
        throw new UnauthorizedException("Only doctors and admins can perform this action");
    }

    private void ensureEmailVerified() {
        String email = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Please verify your email to create or update appointments");
        }
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