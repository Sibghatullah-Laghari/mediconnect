package com.mediconnect.service;

import com.mediconnect.dto.appointment.AppointmentResponse;
import com.mediconnect.dto.appointment.CreateAppointmentRequest;
import com.mediconnect.model.AppointmentStatus;

public interface AppointmentService {
    AppointmentResponse createAppointment(CreateAppointmentRequest request);
    AppointmentResponse getAppointmentById(Long id);
    AppointmentResponse updateStatus(Long id, AppointmentStatus newStatus);
    void deleteAppointment(Long id);
}
