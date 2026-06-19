package com.mediconnect.service;

import com.mediconnect.dto.doctor.CreateDoctorRequest;
import com.mediconnect.dto.doctor.DoctorResponse;
import com.mediconnect.exception.DuplicateEmailException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Role;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    @Override
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        ensureCanManageDoctorEmail(request.email());

        if (doctorRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Doctor having " + request.email() + " email already exists");
        }

        Doctor doctor = new Doctor();
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
    public DoctorResponse getCurrentDoctor() {
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        Doctor doctor = doctorRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for current user"));
        return toResponse(doctor);
    }

    @Override
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = getDoctorEntityById(id);
        ensureCanAccessDoctor(doctor);
        return toResponse(doctor);
    }

    @Override
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAllSpecializations() {
        return doctorRepository.findDistinctSpecializations();
    }

    @Override
    public List<DoctorResponse> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DoctorResponse updateDoctor(Long id, CreateDoctorRequest request) {
        Doctor doctor = getDoctorEntityById(id);
        ensureCanAccessDoctor(doctor);
        ensureCanManageDoctorEmail(request.email());

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
    public void deleteDoctor(Long id) {
        SecurityUtils.requireRole(Role.ADMIN);
        doctorRepository.delete(getDoctorEntityById(id));
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

    private Doctor getDoctorEntityById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
    }

    private void ensureCanManageDoctorEmail(String email) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        if (SecurityUtils.hasRole(Role.DOCTOR) && SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(email)) {
            return;
        }

        throw new UnauthorizedException("You can only manage your own doctor profile");
    }

    private void ensureCanAccessDoctor(Doctor doctor) {
        if (SecurityUtils.hasRole(Role.ADMIN) || SecurityUtils.hasRole(Role.PATIENT)) {
            return;
        }

        if (SecurityUtils.hasRole(Role.DOCTOR)
                && SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(doctor.getEmail())) {
            return;
        }

        throw new UnauthorizedException("You do not have permission to access this doctor profile");
    }
}
