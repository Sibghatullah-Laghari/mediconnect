package com.mediconnect.service;

import com.mediconnect.dto.doctor.CreateDoctorRequest;
import com.mediconnect.dto.doctor.DoctorResponse;

public interface DoctorService {
    DoctorResponse createDoctor(CreateDoctorRequest request);
    DoctorResponse getDoctorById(Long id);
    DoctorResponse updateDoctor(Long id, CreateDoctorRequest request);
    void deleteDoctor(Long id);
}
