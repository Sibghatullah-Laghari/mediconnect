package com.mediconnect.service;

import com.mediconnect.dto.patient.CreatePatientRequest;
import com.mediconnect.dto.patient.PatientResponse;
import com.mediconnect.model.Patient;
import com.mediconnect.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientResponse createPatient(CreatePatientRequest request) {
        if (patientRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Patient email already exists");
        }

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

    private PatientResponse toResponse(Patient patient) {
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
}

