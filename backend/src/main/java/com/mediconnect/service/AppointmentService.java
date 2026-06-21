package com.mediconnect.service;

import com.mediconnect.dto.appointment.AppointmentResponse;
import com.mediconnect.dto.appointment.CreateAppointmentRequest;
import com.mediconnect.model.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Interface for appointment management service.
 * Provides methods for CRUD and status management of appointments.
 */
public interface AppointmentService {
    /**
     * Creates a new appointment.
     *
     * @param request the appointment creation details
     * @return the created appointment response
     */
    AppointmentResponse createAppointment(CreateAppointmentRequest request);

    /**
     * Updates an existing appointment.
     *
     * @param id        the appointment ID
     * @param request    the appointment update request
     * @return the updated appointment response
     */
    AppointmentResponse updateAppointment(Long id, CreateAppointmentRequest request);

    /**
     * Retrieves an appointment by ID.
     *
     * @param id the appointment ID
     * @return the appointment response
     */
    AppointmentResponse getAppointmentById(Long id);

    /**
     * Retrieves all appointments.
     *
     * @return a list of all appointment responses
     */
    List<AppointmentResponse> getAllAppointments();
    Page<AppointmentResponse> getAllAppointments(Pageable pageable);

    /**
     * Retrieves appointments by patient ID.
     *
     * @param patientId the patient ID
     * @return a list of appointment responses for the patient
     */
    List<AppointmentResponse> getAppointmentsByPatient(Long patientId);
    Page<AppointmentResponse> getAppointmentsByPatient(Long patientId, Pageable pageable);

    /**
     * Retrieves appointments by doctor ID.
     *
     * @param doctorId the doctor ID
     * @return a list of appointment responses for the doctor
     */
    List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId);
    Page<AppointmentResponse> getAppointmentsByDoctor(Long doctorId, Pageable pageable);

    /**
     * Updates the status of an appointment.
     *
     * @param id        the appointment ID
     * @param newStatus the new status
     * @return the updated appointment response
     */
    AppointmentResponse updateStatus(Long id, AppointmentStatus newStatus);

    /**
     * Confirms an appointment.
     *
     * @param id the appointment ID
     * @return the updated appointment response
     */
    AppointmentResponse confirmAppointment(Long id);

    /**
     * Completes an appointment.
     *
     * @param id the appointment ID
     * @return the updated appointment response
     */
    AppointmentResponse completeAppointment(Long id);

    /**
     * Cancels an appointment.
     *
     * @param id the appointment ID
     */
    AppointmentResponse cancelAppointment(Long id);

    /**
     * Deletes an appointment.
     *
     * @param id the appointment ID
     */
    void deleteAppointment(Long id);
}
