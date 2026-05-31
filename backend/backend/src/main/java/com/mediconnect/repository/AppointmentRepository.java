package com.mediconnect.repository;

import com.mediconnect.model.Appointment;
import com.mediconnect.model.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findAll();

    List<Appointment> findByPatientId(Long PatientId);

    List<Appointment> findByDoctorId(Long DoctorId);

    List<Appointment> findByStatus(AppointmentStatus status);

}

