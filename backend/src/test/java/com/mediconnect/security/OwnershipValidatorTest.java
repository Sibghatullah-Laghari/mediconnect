package com.mediconnect.security;

import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OwnershipValidatorTest {

    private final OwnershipValidator validator = new OwnershipValidator();

    @Test
    void patientCanAccessOwnRecord() {
        User user = user(Role.PATIENT, 5L);
        Patient patient = patient(5L);

        assertThatCode(() -> validator.assertPatientAccess(user, patient)).doesNotThrowAnyException();
    }

    @Test
    void patientCannotAccessOtherRecord() {
        User user = user(Role.PATIENT, 5L);
        Patient patient = patient(99L);

        assertThatThrownBy(() -> validator.assertPatientAccess(user, patient))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void adminCanAccessAnyDoctor() {
        User user = user(Role.ADMIN, 1L);
        Doctor doctor = new Doctor();
        doctor.setUserId(99L);

        assertThatCode(() -> validator.assertDoctorAccess(user, doctor)).doesNotThrowAnyException();
    }

    private User user(Role role, Long id) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        return user;
    }

    private Patient patient(Long userId) {
        Patient patient = new Patient();
        patient.setUserId(userId);
        return patient;
    }
}
