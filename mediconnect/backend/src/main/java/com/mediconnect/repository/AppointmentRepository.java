package com.mediconnect.repository;

import com.mediconnect.model.Appointment;
import com.mediconnect.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByPatientEmail(String email);

    List<Appointment> findByDoctorEmail(String email);

    List<Appointment> findByStatus(AppointmentStatus status);

    boolean existsByPatientIdAndDoctorIdAndAppointmentDate(
            Long patientId, Long doctorId, LocalDate appointmentDate);
}
