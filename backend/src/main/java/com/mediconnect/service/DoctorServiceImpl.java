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

@Service
@Transactional
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = "specializations", allEntries = true),
            @CacheEvict(cacheNames = "doctors", allEntries = true)
    })
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        if (doctorRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException( "Doctor having " + request.email() + "email already exists");
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

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "doctors", key = "#id")
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = getDoctorEntityById(id);
        return toResponse(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DoctorResponse> getAllDoctors(Pageable pageable) {
        return doctorRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "doctors", key = "'spec:' + #specialization")
    public List<DoctorResponse> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "specializations")
    public List<String> getAllSpecializations() {
        return doctorRepository.findDistinctSpecializations();
    }

    @Override
    public DoctorResponse getCurrentDoctor() {
        Doctor doctor = doctorRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found for current user"));
        return toResponse(doctor);
    }

    private Doctor getDoctorEntityById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor", id)
                );
    }

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

    private void ensureCanUseEmail(String email) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        if (!SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(email)) {
            throw new UnauthorizedException("You can only manage your own doctor profile");
        }
    }

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
