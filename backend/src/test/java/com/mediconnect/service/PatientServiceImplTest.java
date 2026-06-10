package com.mediconnect.service;

import com.mediconnect.dto.patient.CreatePatientRequest;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.model.Gender;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.PatientRepository;
import com.mediconnect.security.OwnershipValidator;
import com.mediconnect.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock private PatientRepository patientRepository;
    @Mock private SecurityUtils securityUtils;
    @Mock private OwnershipValidator ownershipValidator;

    @InjectMocks
    private PatientServiceImpl patientService;

    @Test
    void createPatient_rejectsDuplicateProfileForSameUser() {
        User user = new User();
        user.setId(1L);
        user.setRole(Role.PATIENT);

        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(patientRepository.existsByUserId(1L)).thenReturn(true);

        var request = new CreatePatientRequest(
                "Jane", "jane@example.com", "1234567890",
                LocalDate.of(1990, 1, 1), Gender.FEMALE, "Address"
        );

        assertThatThrownBy(() -> patientService.createPatient(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }
}
