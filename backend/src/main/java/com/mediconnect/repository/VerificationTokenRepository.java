package com.mediconnect.repository;

import com.mediconnect.model.VerificationToken;
import com.mediconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    void deleteByUser(User user);

    @Modifying
    @Transactional
    @Query("UPDATE VerificationToken vt SET vt.used = true WHERE vt.token = :token")
    void markAsUsed(@Param("token") String token);
}