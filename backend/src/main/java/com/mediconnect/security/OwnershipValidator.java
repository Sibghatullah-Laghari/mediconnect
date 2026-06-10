package com.mediconnect.security;

import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.Doctor;
import com.mediconnect.model.Patient;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import org.springframework.stereotype.Component;

@Component
public class OwnershipValidator {

    public void assertPatientAccess(User currentUser, Patient patient) {
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (currentUser.getRole() == Role.PATIENT
                && patient.getUserId() != null
                && patient.getUserId().equals(currentUser.getId())) {
            return;
        }
        throw new UnauthorizedException("You do not have access to this patient record");
    }

    public void assertDoctorAccess(User currentUser, Doctor doctor) {
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        if (currentUser.getRole() == Role.DOCTOR
                && doctor.getUserId() != null
                && doctor.getUserId().equals(currentUser.getId())) {
            return;
        }
        throw new UnauthorizedException("You do not have access to this doctor record");
    }

    public void assertAppointmentAccess(User currentUser, Appointment appointment) {
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }
        Patient patient = appointment.getPatient();
        Doctor doctor = appointment.getDoctor();
        if (currentUser.getRole() == Role.PATIENT
                && patient.getUserId() != null
                && patient.getUserId().equals(currentUser.getId())) {
            return;
        }
        if (currentUser.getRole() == Role.DOCTOR
                && doctor.getUserId() != null
                && doctor.getUserId().equals(currentUser.getId())) {
            return;
        }
        throw new UnauthorizedException("You do not have access to this appointment");
    }
}
