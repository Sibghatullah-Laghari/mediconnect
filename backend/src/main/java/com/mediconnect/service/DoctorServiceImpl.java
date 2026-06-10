package com.mediconnect.service;

import com.mediconnect.dto.doctor.CreateDoctorRequest;
import com.mediconnect.dto.doctor.DoctorResponse;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.DuplicateEmailException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.DoctorRepository;
import com.mediconnect.security.OwnershipValidator;
import com.mediconnect.security.SecurityUtils;
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
    private final SecurityUtils securityUtils;
    private final OwnershipValidator ownershipValidator;

    @Override
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        if (currentUser.getRole() == Role.DOCTOR && doctorRepository.findByUserId(currentUser.getId()).isPresent()) {
            throw new BadRequestException("Doctor profile already exists for this account");
        }
        if (doctorRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException( "Doctor having " + request.email() + "email already exists");
        }

        Doctor doctor = new Doctor();
        doctor.setName(request.name());
        doctor.setGender(request.gender());
        doctor.setSpecialization(request.specialization());
        doctor.setPhone(request.phone());
        doctor.setEmail(request.email());
        doctor.setFee(request.fee());
        doctor.setExperience(request.experience());
        if (currentUser.getRole() == Role.DOCTOR) {
            doctor.setUserId(currentUser.getId());
        }

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
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = getDoctorEntityById(id);
        ownershipValidator.assertDoctorAccess(securityUtils.getCurrentUser(), doctor);
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
    public List<DoctorResponse> getDoctorsBySpecialization(String specialization) {
        return doctorRepository.findBySpecialization(specialization)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Doctor getDoctorEntityById(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Doctor", id)
                );
    }

    @Override
    public DoctorResponse updateDoctor(Long id, CreateDoctorRequest request) {
        Doctor doctor = getDoctorEntityById(id);
        ownershipValidator.assertDoctorAccess(securityUtils.getCurrentUser(), doctor);

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
        Doctor doctor = getDoctorEntityById(id);
        doctorRepository.delete(doctor);
    }
}

