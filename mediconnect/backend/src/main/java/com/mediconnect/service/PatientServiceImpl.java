package com.mediconnect.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;

    @Override
    public PatientResponse createPatient(CreatePatientRequest request) {
        ensureCanManagePatientEmail(request.email());

        if (patientRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("Patient having " + request.email() + " email already exists");
        }

        Patient patient = new Patient();
        patient.setName(request.name());
        patient.setEmail(request.email());
        patient.setPhone(request.phone());
        patient.setDateOfBirth(request.dateOfBirth());
        patient.setGender(request.gender());
        patient.setAddress(request.address());

        return toResponse(patientRepository.save(patient));
    }

    @Override
    public PatientResponse getCurrentPatient() {
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        Patient patient = patientRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for current user"));
        return toResponse(patient);
    }

    @Override
    public PatientResponse getPatientById(Long id) {
        Patient patient = getPatientEntityById(id);
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
    public PatientResponse updatePatient(Long id, CreatePatientRequest request) {
        Patient patient = getPatientEntityById(id);
        ensureCanAccessPatient(patient);
        ensureCanManagePatientEmail(request.email());

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
        SecurityUtils.requireRole(Role.ADMIN);
        Patient patient = getPatientEntityById(id);

        if (patient.getAppointments() != null && !patient.getAppointments().isEmpty()) {
            throw new BadRequestException("Cannot delete patient with existing appointments");
        }

        patientRepository.delete(patient);
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

    private Patient getPatientEntityById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
    }

    private void ensureCanManagePatientEmail(String email) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        if (SecurityUtils.hasRole(Role.PATIENT) && SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(email)) {
            return;
        }

        throw new UnauthorizedException("You can only manage your own patient profile");
    }

    private void ensureCanAccessPatient(Patient patient) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return;
        }

        if (SecurityUtils.hasRole(Role.PATIENT)
                && SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(patient.getEmail())) {
            return;
        }

        throw new UnauthorizedException("You do not have permission to access this patient profile");
    }
}
