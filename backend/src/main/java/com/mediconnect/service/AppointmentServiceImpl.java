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
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
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

/**
 * Implementation of the AppointmentService interface.
 * <p>
 * This service handles all appointment-related business logic including creation,
 * retrieval, updates, status transitions, and deletion. It enforces security rules
 * based on user roles (ADMIN, DOCTOR, PATIENT) and ensures that only authorized
 * users can access or modify appointments. It also validates slot availability,
 * future dating, and email verification before allowing operations.
 * </p>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final Clock clock;
    private final UserRepository userRepository;
    private final MeterRegistry meterRegistry;
    private Counter appointmentsCreatedCounter;

    /**
     * Initializes the metrics counter for tracking created appointments.
     * Called after bean construction.
     */
    @PostConstruct
    void initMetrics() {
        this.appointmentsCreatedCounter = Counter.builder("appointments.created")
                .description("Number of appointments created")
                .register(meterRegistry);
    }

    /**
     * Creates a new appointment.
     * <p>
     * Validates that the requested slot is available and in the future, ensures the
     * user is email-verified, retrieves the patient and doctor, checks that the
     * current user is allowed to create the appointment (admin or the patient owner),
     * and persists the new appointment. Increments the metrics counter.
     * </p>
     *
     * @param request the create appointment request
     * @return the created AppointmentResponse
     * @throws ResourceNotFoundException if patient or doctor not found
     * @throws BadRequestException if slot is invalid or not available
     * @throws UnauthorizedException if user lacks permission or email not verified
     */
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

        Appointment saved = appointmentRepository.save(appointment);
        appointmentsCreatedCounter.increment();
        return toResponse(saved);
    }

    /**
     * Updates an existing appointment.
     * <p>
     * Validates the new slot and ensures it is available (excluding the current appointment),
     * checks email verification, retrieves the patient and doctor, verifies that the
     * current user is permitted to modify the appointment, and saves the changes.
     * </p>
     *
     * @param id the ID of the appointment to update
     * @param request the update request containing new details
     * @return the updated AppointmentResponse
     * @throws ResourceNotFoundException if appointment, patient, or doctor not found
     * @throws BadRequestException if the new slot is invalid or unavailable
     * @throws UnauthorizedException if user lacks permission or email not verified
     */
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

    /**
     * Retrieves an appointment by its ID.
     * <p>
     * Validates that the current user is authorized to view the appointment
     * (admin, the associated patient, or the associated doctor).
     * </p>
     *
     * @param id the appointment ID
     * @return the AppointmentResponse
     * @throws ResourceNotFoundException if the appointment does not exist
     * @throws UnauthorizedException if the user lacks access
     */
    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = getAppointmentEntityById(id);
        ensureCanAccessAppointment(appointment);
        return toResponse(appointment);
    }

    /**
     * Retrieves all appointments based on the current user's role.
     * <p>
     * ADMIN sees all appointments. PATIENT sees only their own appointments.
     * DOCTOR sees only appointments assigned to them.
     * </p>
     *
     * @return list of AppointmentResponse objects
     * @throws UnauthorizedException if the user's role is not recognized
     */
    @Override
    @Transactional(readOnly = true)
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

    /**
     * Retrieves all appointments with pagination based on the current user's role.
     * <p>
     * ADMIN sees all appointments (paginated). PATIENT sees only their own appointments.
     * DOCTOR sees only appointments assigned to them.
     * </p>
     *
     * @param pageable pagination information
     * @return a page of AppointmentResponse objects
     * @throws UnauthorizedException if the user's role is not recognized
     */
    @Override
    @Transactional(readOnly = true)
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

    /**
     * Retrieves all appointments for a specific patient.
     * <p>
     * Ensures the current user is authorized (admin or the patient themselves).
     * </p>
     *
     * @param patientId the patient ID
     * @return list of AppointmentResponse for that patient
     * @throws ResourceNotFoundException if patient not found
     * @throws UnauthorizedException if user lacks permission
     */
    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAppointmentsByPatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
        ensureCanAccessPatientAppointments(patient);

        return appointmentRepository.findByPatientId(patientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves appointments for a specific patient with pagination.
     *
     * @param patientId the patient ID
     * @param pageable pagination information
     * @return a page of AppointmentResponse for that patient
     * @throws ResourceNotFoundException if patient not found
     * @throws UnauthorizedException if user lacks permission
     */
    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getAppointmentsByPatient(Long patientId, Pageable pageable) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));
        ensureCanAccessPatientAppointments(patient);

        return appointmentRepository.findByPatientId(patientId, pageable).map(this::toResponse);
    }

    /**
     * Retrieves all appointments for a specific doctor.
     * <p>
     * Ensures the current user is authorized (admin or the doctor themselves).
     * </p>
     *
     * @param doctorId the doctor ID
     * @return list of AppointmentResponse for that doctor
     * @throws ResourceNotFoundException if doctor not found
     * @throws UnauthorizedException if user lacks permission
     */
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

    /**
     * Retrieves appointments for a specific doctor with pagination.
     *
     * @param doctorId the doctor ID
     * @param pageable pagination information
     * @return a page of AppointmentResponse for that doctor
     * @throws ResourceNotFoundException if doctor not found
     * @throws UnauthorizedException if user lacks permission
     */
    @Override
    public Page<AppointmentResponse> getAppointmentsByDoctor(Long doctorId, Pageable pageable) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));
        ensureCanAccessDoctorAppointments(doctor);

        return appointmentRepository.findByDoctorId(doctorId, pageable).map(this::toResponse);
    }

    /**
     * Updates the status of an appointment.
     * <p>
     * Only valid transitions are allowed: PENDING → CONFIRMED or CANCELLED;
     * CONFIRMED → COMPLETED or CANCELLED. Authorization is checked based on
     * the user's role and relation to the appointment.
     * </p>
     *
     * @param id the appointment ID
     * @param newStatus the new status to set
     * @return the updated AppointmentResponse
     * @throws ResourceNotFoundException if appointment not found
     * @throws InvalidStatusTransitionException if transition is invalid
     * @throws UnauthorizedException if user lacks permission
     */
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

    /**
     * Confirms an appointment (sets status to CONFIRMED).
     * <p>
     * Requires a doctor or admin role.
     * </p>
     *
     * @param id the appointment ID
     * @return the updated AppointmentResponse
     */
    @Override
    public AppointmentResponse confirmAppointment(Long id) {
        requireDoctorOrAdmin();
        return updateStatus(id, AppointmentStatus.CONFIRMED);
    }

    /**
     * Completes an appointment (sets status to COMPLETED).
     * <p>
     * Requires a doctor or admin role.
     * </p>
     *
     * @param id the appointment ID
     * @return the updated AppointmentResponse
     */
    @Override
    public AppointmentResponse completeAppointment(Long id) {
        requireDoctorOrAdmin();
        return updateStatus(id, AppointmentStatus.COMPLETED);
    }

    /**
     * Cancels an appointment (sets status to CANCELLED).
     * <p>
     * Allows admin, the assigned doctor, or the patient owner to cancel.
     * </p>
     *
     * @param id the appointment ID
     * @return the updated AppointmentResponse
     * @throws UnauthorizedException if user lacks permission
     */
    @Override
    public AppointmentResponse cancelAppointment(Long id) {
        Appointment appointment = getAppointmentEntityById(id);
        ensureCanCancelAppointment(appointment);
        return updateStatus(id, AppointmentStatus.CANCELLED);
    }

    /**
     * Deletes an appointment permanently.
     * <p>
     * Only admin or the user who owns the appointment (patient or doctor) can delete.
     * </p>
     *
     * @param id the appointment ID
     * @throws ResourceNotFoundException if appointment not found
     * @throws UnauthorizedException if user lacks permission
     */
    @Override
    public void deleteAppointment(Long id) {
        Appointment appointment = getAppointmentEntityById(id);
        ensureCanModifyAppointment(appointment);
        appointmentRepository.delete(appointment);
    }

    /**
     * Checks if an appointment slot is bookable (future date/time).
     *
     * @param appointmentDate the date
     * @param appointmentTime the time
     * @return true if the slot is in the future, false otherwise
     */
    public boolean canBookAppointmentSlot(LocalDate appointmentDate, LocalTime appointmentTime) {
        if (appointmentDate == null || appointmentTime == null) {
            return false;
        }

        LocalDateTime requestedSlot = LocalDateTime.of(appointmentDate, appointmentTime);
        return requestedSlot.isAfter(LocalDateTime.now(clock));
    }

    /**
     * Validates that the requested appointment slot is in the future and that
     * the doctor is available at that time (no conflicting appointments).
     *
     * @param request the appointment request
     * @param excludedAppointmentId if updating, the ID of the appointment to exclude from conflict check
     * @throws BadRequestException if slot is in the past or conflict exists
     */
    public void validateBookableAppointmentSlot(CreateAppointmentRequest request, Long excludedAppointmentId) {
        if (request == null) {
            throw new BadRequestException("Appointment request is required");
        }

        if (!canBookAppointmentSlot(request.appointmentDate(), request.appointmentTime())) {
            throw new BadRequestException("Appointment must be scheduled in the future");
        }

        validateDoctorAvailability(request.doctorId(), request.appointmentDate(), request.appointmentTime(), excludedAppointmentId);
    }

    // ----- PRIVATE HELPER METHODS -----

    /**
     * Retrieves an appointment entity by ID or throws ResourceNotFoundException.
     */
    private Appointment getAppointmentEntityById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", id));
    }

    /**
     * Converts an Appointment entity to an AppointmentResponse DTO.
     */
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

    /**
     * Validates that the doctor does not have a conflicting appointment at the given date/time.
     * If excludedAppointmentId is provided, that appointment is ignored (for updates).
     *
     * @throws AppointmentConflictException if a conflict exists
     */
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

    /**
     * Ensures that the current user is allowed to create an appointment for the given patient.
     * Admin can create for anyone; a patient can only create for themselves.
     *
     * @throws UnauthorizedException if not allowed
     */
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

    /**
     * Ensures the current user can view the appointment: admin, the patient, or the doctor.
     *
     * @throws UnauthorizedException if not allowed
     */
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

    /**
     * Ensures the current user has permission to change the appointment status.
     * For CONFIRMED/COMPLETED: only doctor or admin. For CANCELLED: doctor, patient, or admin.
     *
     * @param newStatus the status being set
     * @throws UnauthorizedException if not allowed
     */
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

    /**
     * Ensures the current user can cancel the appointment: admin, doctor, or patient.
     *
     * @throws UnauthorizedException if not allowed
     */
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

    /**
     * Throws UnauthorizedException if the current user is not a doctor or admin.
     */
    private void requireDoctorOrAdmin() {
        if (SecurityUtils.hasRole(Role.DOCTOR) || SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }
        throw new UnauthorizedException("Only doctors and admins can perform this action");
    }

    /**
     * Ensures the currently authenticated user has a verified email address.
     *
     * @throws UnauthorizedException if email is not verified
     */
    private void ensureEmailVerified() {
        String email = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Please verify your email to create or update appointments");
        }
    }

    /**
     * Ensures the current user can modify (update/delete) the appointment:
     * admin, the patient, or the doctor associated.
     *
     * @throws UnauthorizedException if not allowed
     */
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

    /**
     * Ensures the current user can view appointments for the given patient:
     * admin or the patient themselves.
     *
     * @throws UnauthorizedException if not allowed
     */
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

    /**
     * Ensures the current user can view appointments for the given doctor:
     * admin or the doctor themselves.
     *
     * @throws UnauthorizedException if not allowed
     */
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