package com.mediconnect.service;

import com.mediconnect.dto.appointment.AppointmentResponse;
import com.mediconnect.dto.appointment.CreateAppointmentRequest;
import com.mediconnect.model.AppointmentStatus;
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

    /**
     * Retrieves appointments by patient ID.
     *
     * @param patientId the patient ID
     * @return a list of appointment responses for the patient
     */
    List<AppointmentResponse> getAppointmentsByPatient(Long patientId);

    /**
     * Retrieves appointments by doctor ID.
     *
     * @param doctorId the doctor ID
     * @return a list of appointment responses for the doctor
     */
    List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId);

    /**
     * Updates the status of an appointment.
     *
     * @param id        the appointment ID
     * @param newStatus the new status
     * @return the updated appointment response
     */
    AppointmentResponse updateStatus(Long id, AppointmentStatus newStatus);

    /**
     * Cancels an appointment.
     *
     * @param id the appointment ID
     */
    void cancelAppointment(Long id);

    /**
     * Deletes an appointment.
     *
     * @param id the appointment ID
     */
    void deleteAppointment(Long id);
}
