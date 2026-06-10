package com.mediconnect.controller;

import com.mediconnect.dto.patient.CreatePatientRequest;
import com.mediconnect.dto.patient.PatientResponse;
import com.mediconnect.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing patients.
 * Provides endpoints for creating, retrieving, updating, and deleting patients.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/patients")
public class PatientController {

    /**
     * Service for handling patient business logic.
     */
    private final PatientService patientService;

    /**
     * Creates a new patient.
     *
     * @param request the patient creation details
     * @return the created patient response
     */
    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(
            @Valid @RequestBody CreatePatientRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(patientService.createPatient(request));
    }

    /**
     * Retrieves a patient by ID.
     *
     * @param id the patient ID
     * @return the patient response
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<PatientResponse> getPatientById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    /**
     * Retrieves all patients with pagination.
     *
     * @param page the page number
     * @param size the page size
     * @return a page of patient responses
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PatientResponse>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(patientService.getAllPatients(pageable));
    }

    /**
     * Updates an existing patient.
     *
     * @param id      the patient ID
     * @param request the patient update details
     * @return the updated patient response
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody CreatePatientRequest request
    ) {
        return ResponseEntity.ok(patientService.updatePatient(id, request));
    }

    /**
     * Deletes a patient by ID.
     *
     * @param id the patient ID
     * @return a response entity
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}