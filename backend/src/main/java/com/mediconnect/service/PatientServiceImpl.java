package com.mediconnect.service;

import com.mediconnect.dto.patient.CreatePatientRequest;
import com.mediconnect.dto.patient.PatientResponse;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.DuplicateEmailException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.Patient;
import com.mediconnect.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    public PatientResponse createPatient(CreatePatientRequest request) {
        if (patientRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Patient having " + request.email() + "email already exists");
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

    @Override
    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        return toResponse(patient);
    }

    private Patient getPatientEntityById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    @Override
    public PatientResponse updatePatient(Long id, CreatePatientRequest request) {
        Patient patient = getPatientEntityById(id);
        
        patient.setName(request.name());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setAddress(request.address());

        return toResponse(patientRepository.save(patient));
    }

    @Override
    public void deletePatient(Long id) {
        // Fetch the patient or throw if not found
        Patient patient = getPatientEntityById(id);

        // If patient has any appointments, prevent deletion
        if (patient.getAppointments() != null && !patient.getAppointments().isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete patient with existing appointments"
            );
        }

        // Safe to delete
        patientRepository.delete(patient);
    }
}

