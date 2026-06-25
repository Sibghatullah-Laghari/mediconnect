package com.mediconnect.repository;

import com.mediconnect.dto.appointment.AppointmentAvailabilityProjection;
import com.mediconnect.dto.appointment.AppointmentDetailsProjection;
import com.mediconnect.model.Appointment;
import com.mediconnect.model.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Appointment entities.
 *
 * <p>Provides CRUD operations and custom query methods for
 * retrieving appointments by patient, doctor, status, and date.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE a.patient.id = :patientId")
    List<Appointment> findByPatientId(Long patientId);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE a.doctor.id = :doctorId")
    List<Appointment> findByDoctorId(Long doctorId);

    @Query(value = "SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE a.patient.id = :patientId",
           countQuery = "SELECT count(a) FROM Appointment a WHERE a.patient.id = :patientId")
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    @Query(value = "SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE a.doctor.id = :doctorId",
           countQuery = "SELECT count(a) FROM Appointment a WHERE a.doctor.id = :doctorId")
    Page<Appointment> findByDoctorId(Long doctorId, Pageable pageable);

    @Query(value = "SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE a.patient.email = :email",
           countQuery = "SELECT count(a) FROM Appointment a WHERE a.patient.email = :email")
    Page<Appointment> findByPatientEmail(String email, Pageable pageable);

    @Query(value = "SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE a.doctor.email = :email",
           countQuery = "SELECT count(a) FROM Appointment a WHERE a.doctor.email = :email")
    Page<Appointment> findByDoctorEmail(String email, Pageable pageable);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE a.patient.email = :email")
    List<Appointment> findByPatientEmail(String email);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE a.doctor.email = :email")
    List<Appointment> findByDoctorEmail(String email);

    @Query("SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor WHERE a.status = :status")
    List<Appointment> findByStatus(AppointmentStatus status);

    @Query(value = "SELECT a FROM Appointment a JOIN FETCH a.patient JOIN FETCH a.doctor",
           countQuery = "SELECT count(a) FROM Appointment a")
    Page<Appointment> findAllWithDetails(Pageable pageable);

    boolean existsByPatientIdAndDoctorIdAndAppointmentDate(
            Long patientId,
            Long doctorId,
            LocalDate appointmentDate
    );

    @Query("SELECT a.id as id, a.status as status, a.reason as reason, a.appointmentDate as appointmentDate, a.appointmentTime as appointmentTime, a.patient.id as patientId, a.patient.name as patientName, a.doctor.id as doctorId, a.doctor.name as doctorName FROM Appointment a")
    Page<AppointmentDetailsProjection> findAllProjectedBy(Pageable pageable);

    @Query("SELECT a.id as id, a.status as status, a.reason as reason, a.appointmentDate as appointmentDate, a.appointmentTime as appointmentTime, a.patient.id as patientId, a.patient.name as patientName, a.doctor.id as doctorId, a.doctor.name as doctorName FROM Appointment a WHERE a.patient.id = :patientId")
    Page<AppointmentDetailsProjection> findByPatientIdProjectedBy(Long patientId, Pageable pageable);

    @Query("SELECT a.id as id, a.status as status, a.reason as reason, a.appointmentDate as appointmentDate, a.appointmentTime as appointmentTime, a.patient.id as patientId, a.patient.name as patientName, a.doctor.id as doctorId, a.doctor.name as doctorName FROM Appointment a WHERE a.doctor.id = :doctorId")
    Page<AppointmentDetailsProjection> findByDoctorIdProjectedBy(Long doctorId, Pageable pageable);

    @Query("SELECT a.id as id, a.status as status, a.reason as reason, a.appointmentDate as appointmentDate, a.appointmentTime as appointmentTime, a.patient.id as patientId, a.patient.name as patientName, a.doctor.id as doctorId, a.doctor.name as doctorName FROM Appointment a WHERE a.id = :id")
    Optional<AppointmentDetailsProjection> findProjectedById(Long id);

    @Query("SELECT a.id as id, a.appointmentDate as appointmentDate, a.appointmentTime as appointmentTime FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :appointmentDate")
    List<AppointmentAvailabilityProjection> findByDoctorIdAndAppointmentDateProjectedBy(Long doctorId, LocalDate appointmentDate);
}