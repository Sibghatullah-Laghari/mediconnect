package com.mediconnect.controller;

import com.mediconnect.dto.appointment.AppointmentResponse;
import com.mediconnect.dto.appointment.CreateAppointmentRequest;
import com.mediconnect.dto.appointment.StatusRequest;
import com.mediconnect.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing appointments.
 * Provides endpoints for creating, retrieving, updating, and deleting appointments..
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/appointments")
public class AppointmentController {

    /**
     * Service for handling appointment business logic.
     */
    private final AppointmentService appointmentService;

    /**
     * Creates a new appointment.
     * This endpoint is used to create a new appointment.
     * @param request the appointment creation details
     * @return the created appointment response
     */
    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, request));
    }

    /**
     * Retrieves an appointment by ID.
     *
     * @param id the appointment ID
     * @return the appointment response
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    /**
     * Retrieves all appointments.
     *
     * @return a list of all appointment responses
     */
    @GetMapping
    public ResponseEntity<Page<AppointmentResponse>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments(Pageable.of(page, size)));
    }

    @GetMapping(params = {"page", "size"})
    public ResponseEntity<Page<AppointmentResponse>> getAllAppointmentsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(appointmentService.getAllAppointments(PageRequest.of(page, size)));
    }

    /**
     * Retrieves all appointments for a given patient.
     *
     * @param patientId the patient ID
     * @return a list of appointment responses for the patient
     */
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId));
    }

    @GetMapping(value = "/patient/{patientId}", params = {"page", "size"})
    public ResponseEntity<Page<AppointmentResponse>> getAppointmentsByPatientPaginated(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByPatient(patientId, PageRequest.of(page, size)));
    }

    /**
     * Retrieves all appointments for a given doctor.
     *
     * @param doctorId the doctor ID
     * @return a list of appointment responses for the doctor
     */
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<AppointmentResponse>> getAppointmentsByDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId));
    }

    @GetMapping(value = "/doctor/{doctorId}", params = {"page", "size"})
    public ResponseEntity<Page<AppointmentResponse>> getAppointmentsByDoctorPaginated(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctor(doctorId, PageRequest.of(page, size)));
    }

    /**
     * Updates the status of an appointment.
     *
     * @param id      the appointment ID
     * @param request the status update request
     * @return the updated appointment response
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, request.status()));
    }

    @PatchMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirmAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.confirmAppointment(id));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.completeAppointment(id));
    }

    /**
     * Cancels an appointment by ID.
     *
     * @param id the appointment ID
     * @return a response entity
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id));
    }

    /**
     * Deletes an appointment by ID.
     *
     * @param id the appointment ID
     * @return a response entity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
