package com.mediconnect.service;
 
import com.mediconnect.dto.doctor.CreateDoctorRequest;
import com.mediconnect.dto.doctor.DoctorResponse;
import java.util.List;

public interface DoctorService {
    DoctorResponse createDoctor(CreateDoctorRequest request);
    DoctorResponse getCurrentDoctor();
    DoctorResponse getDoctorById(Long id);
    List<DoctorResponse> getAllDoctors();
    List<String> getAllSpecializations();
    List<DoctorResponse> getDoctorsBySpecialization(String specialization);
    DoctorResponse updateDoctor(Long id, CreateDoctorRequest request);
    void deleteDoctor(Long id);
}
