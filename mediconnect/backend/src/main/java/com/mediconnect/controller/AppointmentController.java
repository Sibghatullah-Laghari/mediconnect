package com.mediconnect.controller;

import com.mediconnect.dto.appointment.AppointmentResponse;
import com.mediconnect.dto.appointment.CreateAppointmentRequest;
import com.mediconnect.dto.appointment.StatusRequest;
import com.mediconnect.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing appointments.
 * Provides endpoints for creating, retrieving, updating, and deleting appointments.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/appointments")
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
    public ResponseEntity<List<AppointmentResponse>> getAllAppointments() {
        return ResponseEntity.ok(appointmentService.getAllAppointments());
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

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentResponse> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody CreateAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.updateAppointment(id, request));
    }

    /**
     * Updates the status of an appointment.
     *
     * @param id      the appointment ID
     * @param request the status update request
     * @return the updated appointment response
     */
    @PatchMapping("/{id}/status")
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
