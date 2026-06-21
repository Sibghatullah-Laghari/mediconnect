package com.mediconnect.service;
 
import com.mediconnect.dto.doctor.CreateDoctorRequest;
import com.mediconnect.dto.doctor.DoctorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface DoctorService {
    DoctorResponse createDoctor(CreateDoctorRequest request);
    DoctorResponse getCurrentDoctor();
    DoctorResponse getDoctorById(Long id);
    List<DoctorResponse> getAllDoctors();
    Page<DoctorResponse> getAllDoctors(Pageable pageable);
    List<DoctorResponse> getDoctorsBySpecialization(String specialization);
    List<String> getAllSpecializations();
    DoctorResponse updateDoctor(Long id, CreateDoctorRequest request);
    void deleteDoctor(Long id);
}
