package com.mediconnect.controller;

import com.mediconnect.dto.patient.CreatePatientRequest;
import com.mediconnect.dto.patient.PatientResponse;
import com.mediconnect.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing patients.
 * Provides endpoints for creating, retrieving, updating, and deleting patients.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/patients")
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

    @GetMapping("/me")
    public ResponseEntity<PatientResponse> getCurrentPatient() {
        return ResponseEntity.ok(patientService.getCurrentPatient());
    }

    /**
     * Retrieves a patient by ID.
     *
     * @param id the patient ID
     * @return the patient response
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getPatientById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    /**
     * Retrieves all patients.
     *
     * @return a list of all patient responses
     */
    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @GetMapping(params = {"page", "size"})
    public ResponseEntity<Page<PatientResponse>> getAllPatientsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(patientService.getAllPatients(PageRequest.of(page, size)));
    }

    /**
     * Updates an existing patient.
     *
     * @param id      the patient ID
     * @param request the patient update details
     * @return the updated patient response
     */
    @PutMapping("/{id}")
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
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
