package com.mediconnect.service;

import java.util.List;
import java.util.stream.Collectors;
import com.mediconnect.dto.patient.CreatePatientRequest;
import com.mediconnect.dto.patient.PatientResponse;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.DuplicateEmailException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.model.Patient;
import com.mediconnect.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    public PatientResponse createPatient(CreatePatientRequest request) {
        log.info("Creating patient with email: {}", request.email());
        if (patientRepository.existsByEmail(request.email())) {
            log.warn("Duplicate email attempt: {}", request.email());
            throw new DuplicateEmailException("Patient having " + request.email() + " email already exists");
        }

        Patient patient = new Patient();
        patient.setName(request.name());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setAddress(request.address());

        Patient saved = patientRepository.save(patient);
        log.info("Patient created successfully with id: {}, email: {}", saved.getId(), saved.getEmail());
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
        log.debug("Fetching patient with id: {}", id);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Patient not found with id: {}", id);
                    return new ResourceNotFoundException("Patient", id);
                });
        log.debug("Patient found: {}", patient.getEmail());
        return toResponse(patient);
    }

    @Override
    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        log.info("Fetching all patients with pagination: {}", pageable);
        return patientRepository.findAll(pageable)
                .map(this::toResponse);
    }

    private Patient getPatientEntityById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    @Override
    public PatientResponse updatePatient(Long id, CreatePatientRequest request) {
        log.info("Updating patient with id: {}", id);
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
        log.warn("Soft deleting patient with id: {}", id);
        Patient patient = getPatientEntityById(id);

        if (patient.getAppointments() != null && !patient.getAppointments().isEmpty()) {
            log.error("Cannot delete patient id: {} with {} appointments", id, patient.getAppointments().size());
            throw new BadRequestException("Cannot delete patient with existing appointments");
        }

        patient.setIsDeleted(true);
        patient.setDeletedAt(LocalDateTime.now());
        patientRepository.save(patient);
        log.info("Patient soft deleted successfully: id: {}", id);
    }
}

