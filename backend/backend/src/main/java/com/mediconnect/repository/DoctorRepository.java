package com.mediconnect.repository;

import com.mediconnect.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    boolean existsByEmail(String email);
    Optional<Doctor> findByEmail(String Email);
}

