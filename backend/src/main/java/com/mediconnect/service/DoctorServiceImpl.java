package com.mediconnect.service;

import com.mediconnect.dto.doctor.CreateDoctorRequest;
import com.mediconnect.dto.doctor.DoctorResponse;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.DuplicateEmailException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Role;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing doctor profiles.
 * <p>
 * Provides CRUD operations for doctors with caching, role-based authorization,
 * and email uniqueness enforcement. Caches are evicted appropriately on changes
 * to keep data consistent.
 * </p>
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    /**
     * Creates a new doctor profile.
     * <p>
     * Validates that the email is not already used by another doctor and that the
     * current user is authorized (admin or the doctor themselves). Evicts all
     * doctor and specialization caches upon successful creation.
     * </p>
     *
     * @param request the doctor creation request
     * @return the created DoctorResponse
     * @throws DuplicateEmailException if the email already exists
     * @throws UnauthorizedException if the user lacks permission
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "specializations", allEntries = true),
            @CacheEvict(cacheNames = "doctors", allEntries = true)
    })
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        if (doctorRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Doctor having " + request.email() + "email already exists");
        }

        ensureCanUseEmail(request.email());

        Doctor doctor = new Doctor();
        doctor.setName(request.name());
        doctor.setGender(request.gender());
        doctor.setSpecialization(request.specialization());
        doctor.setPhone(request.phone());
        doctor.setEmail(request.email());
        doctor.setFee(request.fee());
        doctor.setExperience(request.experience());

        Doctor saved = doctorRepository.save(doctor);
        return toResponse(saved);
    }

    /**
     * Converts a Doctor entity to a DoctorResponse DTO.
     *
     * @param doctor the doctor entity
     * @return the response DTO
     */
    private DoctorResponse toResponse(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getGender(),
                doctor.getSpecialization(),
                doctor.getPhone(),
                doctor.getEmail(),
                doctor.getFee(),
                doctor.getExperience()
        );
    }

    /**
     * Retrieves a doctor by ID with caching.
     *
     * @param id the doctor ID
     * @return the DoctorResponse
     * @throws ResourceNotFoundException if the doctor does not exist
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "doctors", key = "#id")
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = getDoctorEntityById(id);
        return toResponse(doctor);
    }

    /**
     * Retrieves all doctors (unpaged).
     *
     * @return list of all DoctorResponse objects
     */
    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all doctors with pagination.
     *
     * @param pageable pagination information
     * @return a page of DoctorResponse objects
     */
    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAllDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(this::toResponse);
    }

    /**
     * Retrieves doctors by their specialization with caching.
     *
     * @param specialization the specialization to filter by
     * @return list of DoctorResponse objects
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "doctors", key = "'spec:' + #specialization")
    public List<DoctorResponse> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all distinct specializations with caching.
     *
     * @return list of specialization strings
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "specializations")
    public List<String> getAllSpecializations() {
        return doctorRepository.findDistinctSpecializations();
    }

    /**
     * Retrieves the doctor profile of the currently authenticated user.
     *
     * @return the DoctorResponse of the current doctor
     * @throws ResourceNotFoundException if no doctor is associated with the current user's email
     */
    @Override
    public DoctorResponse getCurrentDoctor() {
        Doctor doctor = doctorRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found for current user"));
        return toResponse(doctor);
    }

    /**
     * Retrieves a doctor entity by ID or throws ResourceNotFoundException.
     *
     * @param id the doctor ID
     * @return the Doctor entity
     */
    private Doctor getDoctorEntityById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor", id)
                );
    }

    /**
     * Updates an existing doctor profile.
     * <p>
     * Ensures the current user is authorized (admin or the doctor themselves),
     * validates email uniqueness, and evicts relevant caches for the updated
     * doctor, affected specialization, and all specializations.
     * </p>
     *
     * @param id the ID of the doctor to update
     * @param request the update request
     * @return the updated DoctorResponse
     * @throws ResourceNotFoundException if doctor not found
     * @throws DuplicateEmailException if the new email is already used by another doctor
     * @throws UnauthorizedException if the user lacks permission
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "doctors", key = "#id"),
            @CacheEvict(cacheNames = "doctors", key = "'spec:' + #request.specialization()", condition = "#request != null && #request.specialization() != null"),
            @CacheEvict(cacheNames = "specializations", allEntries = true)
    })
    public DoctorResponse updateDoctor(Long id, CreateDoctorRequest request) {
        Doctor doctor = getDoctorEntityById(id);

        ensureCanAccessDoctor(doctor);

        if (doctorRepository.existsByEmail(request.email()) && !doctor.getEmail().equalsIgnoreCase(request.email())) {
            throw new DuplicateEmailException("Doctor having " + request.email() + "email already exists");
        }

        doctor.setName(request.name());
        doctor.setGender(request.gender());
        doctor.setSpecialization(request.specialization());
        doctor.setPhone(request.phone());
        doctor.setEmail(request.email());
        doctor.setFee(request.fee());
        doctor.setExperience(request.experience());

        return toResponse(doctorRepository.save(doctor));
    }

    /**
     * Deletes a doctor profile.
     * <p>
     * Ensures the current user is authorized, and that the doctor has no existing
     * appointments before deletion. Evicts the specific doctor cache and all
     * specializations.
     * </p>
     *
     * @param id the ID of the doctor to delete
     * @throws ResourceNotFoundException if doctor not found
     * @throws BadRequestException if the doctor has existing appointments
     * @throws UnauthorizedException if the user lacks permission
     */
    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "doctors", key = "#id"),
            @CacheEvict(cacheNames = "specializations", allEntries = true)
    })
    public void deleteDoctor(Long id) {
        Doctor doctor = getDoctorEntityById(id);
        ensureCanAccessDoctor(doctor);

        if (doctor.getAppointments() != null && !doctor.getAppointments().isEmpty()) {
            throw new BadRequestException("Cannot delete doctor with existing appointments");
        }

        doctorRepository.delete(doctor);
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
            throw new UnauthorizedException("You can only manage your own doctor profile");
        }
    }

    /**
     * Ensures that the current user has access to the given doctor profile.
     * <p>
     * Admins have full access; other users can only access their own profile.
     * </p>
     *
     * @param doctor the doctor to check
     * @throws UnauthorizedException if the user lacks permission
     */
    private void ensureCanAccessDoctor(Doctor doctor) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        if (SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(doctor.getEmail())) {
            return;
        }

        throw new UnauthorizedException("You do not have permission to access this doctor profile");
    }
}