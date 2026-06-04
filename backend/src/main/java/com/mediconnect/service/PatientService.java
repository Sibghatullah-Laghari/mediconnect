package com.mediconnect.service;

import com.mediconnect.dto.patient.CreatePatientRequest;
import com.mediconnect.dto.patient.PatientResponse;

public interface PatientService {
    PatientResponse createPatient(CreatePatientRequest request);
    PatientResponse getPatientById(Long id);
    PatientResponse updatePatient(Long id, CreatePatientRequest request);
    void deletePatient(Long id);
}
