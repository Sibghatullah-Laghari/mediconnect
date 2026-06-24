package com.mediconnect.repository;

import com.mediconnect.model.Appointment;
import com.mediconnect.model.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing Appointment entities.
 *
 * <p>Provides CRUD operations and custom query methods for
 * retrieving appointments by patient, doctor, status, and date.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    Page<Appointment> findByPatientEmail(String email, Pageable pageable);

    Page<Appointment> findByDoctorEmail(String email, Pageable pageable);

    List<Appointment> findByPatientEmail(String email);

    List<Appointment> findByDoctorEmail(String email);

    List<Appointment> findByStatus(AppointmentStatus status);

    boolean existsByPatientIdAndDoctorIdAndAppointmentDate(
            Long patientId,
            Long doctorId,
            LocalDate appointmentDate
    );
}