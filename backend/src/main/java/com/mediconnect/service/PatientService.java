package com.mediconnect.service;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.mediconnect.dto.patient.CreatePatientRequest;
import com.mediconnect.dto.patient.PatientResponse;

public interface PatientService {
    PatientResponse createPatient(CreatePatientRequest request);
    PatientResponse getCurrentPatient();
    PatientResponse getPatientById(Long id);
    List<PatientResponse> getAllPatients();
    Page<PatientResponse> getAllPatients(Pageable pageable);
    PatientResponse updatePatient(Long id, CreatePatientRequest request);
    void deletePatient(Long id);
}
