package com.mediconnect.service;

import com.mediconnect.dto.doctor.CreateDoctorRequest;
import com.mediconnect.dto.doctor.DoctorResponse;
import com.mediconnect.exception.DuplicateEmailException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.Doctor;
import com.mediconnect.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorResponse createDoctor(CreateDoctorRequest request) {
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
        return toResponse(doctor);
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

