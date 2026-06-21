package com.mediconnect.repository;

import com.mediconnect.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Doctor> findBySpecialization(String specialization);

    @Query("SELECT DISTINCT d.specialization FROM Doctor d ORDER BY d.specialization ASC")
    List<String> findDistinctSpecializations();

    List<Doctor> findByNameContainingIgnoreCase(String name);

    Page<Doctor> findAll(Pageable pageable);

    @Query("SELECT d FROM Doctor d " +
            "LEFT JOIN FETCH d.appointments " +
            "WHERE d.id = :id")
    Optional<Doctor> findByIdWithAppointments(@Param("id") Long id);
}
