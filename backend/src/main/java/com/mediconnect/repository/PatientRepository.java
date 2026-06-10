package com.mediconnect.repository;

import com.mediconnect.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Patient> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<Patient> findByNameContainingIgnoreCase(String name);

    Page<Patient> findAll(Pageable pageable);

    @Query("SELECT p FROM Patient p " +
            "LEFT JOIN FETCH p.appointments " +
            "WHERE p.id = :id")
    Optional<Patient> findByIdWithAppointments(@Param("id") Long id);
}