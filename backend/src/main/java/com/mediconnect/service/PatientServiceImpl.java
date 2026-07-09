package com.mediconnect.service;

import java.util.List;
import java.util.stream.Collectors;
import com.mediconnect.dto.patient.CreatePatientRequest;
import com.mediconnect.dto.patient.PatientResponse;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.DuplicateEmailException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Role;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for managing patient profiles.
 * <p>
 * Provides CRUD operations for patients with role‑based authorization.
 * Only admins can view all patients; patients can only access and manage
 * their own profiles. Email uniqueness is enforced, and deletion is prevented
 * if the patient has associated appointments.
 * </p>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    /**
     * Creates a new patient profile.
     * <p>
     * Ensures the email is not already used and that the current user is authorized
     * (admin or the patient themselves). Then persists the patient entity.
     * </p>
     *
     * @param request the patient creation request
     * @return the created PatientResponse
     * @throws DuplicateEmailException if the email already exists
     * @throws UnauthorizedException if the user lacks permission
     */
    @Override
    public PatientResponse createPatient(CreatePatientRequest request) {
        if (patientRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Patient having " + request.email() + "email already exists");
        }

        ensureCanUseEmail(request.email());

        Patient patient = new Patient();
        patient.setName(request.name());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setAddress(request.address());

        Patient saved = patientRepository.save(patient);
        return toResponse(saved);
    }

    /**
     * Retrieves the patient profile of the currently authenticated user.
     *
     * @return the PatientResponse of the current patient
     * @throws ResourceNotFoundException if no patient is associated with the current user's email
     */
    @Override
    public PatientResponse getCurrentPatient() {
        Patient patient = patientRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found for current user"));
        return toResponse(patient);
    }

    /**
     * Converts a Patient entity to a PatientResponse DTO.
     *
     * @param patient the patient entity
     * @return the response DTO
     */
    public PatientResponse toResponse(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getName(),
                patient.getEmail(),
                patient.getPhone(),
                patient.getDateOfBirth(),
                patient.getGender(),
                patient.getAddress()
        );
    }

    /**
     * Retrieves a patient by ID with authorization.
     * <p>
     * Ensures the current user is admin or the patient themselves.
     * </p>
     *
     * @param id the patient ID
     * @return the PatientResponse
     * @throws ResourceNotFoundException if the patient does not exist
     * @throws UnauthorizedException if the user lacks access
     */
    @Override
    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        ensureCanAccessPatient(patient);
        return toResponse(patient);
    }

    /**
     * Retrieves all patients (unpaged). Admin only.
     *
     * @return list of all PatientResponse objects
     * @throws UnauthorizedException if the current user is not an admin
     */
    @Override
    public List<PatientResponse> getAllPatients() {
        SecurityUtils.requireRole(Role.ADMIN);

        return patientRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all patients with pagination. Admin only.
     *
     * @param pageable pagination information
     * @return a page of PatientResponse objects
     * @throws UnauthorizedException if the current user is not an admin
     */
    @Override
    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        SecurityUtils.requireRole(Role.ADMIN);

        return patientRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * Retrieves a patient entity by ID or throws ResourceNotFoundException.
     *
     * @param id the patient ID
     * @return the Patient entity
     */
    private Patient getPatientEntityById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    /**
     * Updates an existing patient profile.
     * <p>
     * Ensures the current user is authorized (admin or the patient themselves),
     * validates email uniqueness (if changed), and updates the entity.
     * </p>
     *
     * @param id the ID of the patient to update
     * @param request the update request
     * @return the updated PatientResponse
     * @throws ResourceNotFoundException if patient not found
     * @throws DuplicateEmailException if the new email is already used by another patient
     * @throws UnauthorizedException if the user lacks permission
     */
    @Override
    public PatientResponse updatePatient(Long id, CreatePatientRequest request) {
        Patient patient = getPatientEntityById(id);

        ensureCanAccessPatient(patient);

        if (patientRepository.existsByEmail(request.email()) && !patient.getEmail().equalsIgnoreCase(request.email())) {
            throw new DuplicateEmailException("Patient having " + request.email() + "email already exists");
        }

        patient.setName(request.name());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setAddress(request.address());

        return toResponse(patientRepository.save(patient));
    }

    /**
     * Deletes a patient profile.
     * <p>
     * Ensures the current user is authorized (admin or the patient themselves)
     * and that the patient has no existing appointments before deletion.
     * </p>
     *
     * @param id the ID of the patient to delete
     * @throws ResourceNotFoundException if patient not found
     * @throws BadRequestException if the patient has existing appointments
     * @throws UnauthorizedException if the user lacks permission
     */
    @Override
    public void deletePatient(Long id) {
        // Fetch the patient or throw if not found
        Patient patient = getPatientEntityById(id);
        ensureCanAccessPatient(patient);

        // If patient has any appointments, prevent deletion
        if (patient.getAppointments() != null && !patient.getAppointments().isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete patient with existing appointments"
            );
        }

        // Safe to delete
        patientRepository.delete(patient);
    }

    /**
     * Ensures that the current user is allowed to use the given email address.
     * <p>
     * Admins can use any email; non‑admins can only use their own email.
     * </p>
     *
     * @param email the email to check
     * @throws UnauthorizedException if the user is not allowed
     */
    private void ensureCanUseEmail(String email) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        if (!SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(email)) {
            throw new UnauthorizedException("You can only manage your own patient profile");
        }
    }

    /**
     * Ensures that the current user has access to the given patient profile.
     * <p>
     * Admins have full access; other users can only access their own profile.
     * </p>
     *
     * @param patient the patient to check
     * @throws UnauthorizedException if the user lacks permission
     */
    private void ensureCanAccessPatient(Patient patient) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        if (SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(patient.getEmail())) {
            return;
        }

        throw new UnauthorizedException("You do not have permission to access this patient profile");
    }
}