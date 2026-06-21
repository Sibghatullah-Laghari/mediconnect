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

@Service
@Transactional
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

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

    @Override
    public PatientResponse getCurrentPatient() {
        Patient patient = patientRepository.findByEmail(SecurityUtils.getCurrentUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found for current user"));
        return toResponse(patient);
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
        ensureCanAccessPatient(patient);
        return toResponse(patient);
    }

    @Override
    public List<PatientResponse> getAllPatients() {
        SecurityUtils.requireRole(Role.ADMIN);

        return patientRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        SecurityUtils.requireRole(Role.ADMIN);

        return patientRepository.findAll(pageable).map(this::toResponse);
    }

    private Patient getPatientEntityById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

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

    private void ensureCanUseEmail(String email) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        if (!SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(email)) {
            throw new UnauthorizedException("You can only manage your own patient profile");
        }
    }

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
