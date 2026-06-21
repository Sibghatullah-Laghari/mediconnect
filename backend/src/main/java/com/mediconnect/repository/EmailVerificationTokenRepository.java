package com.mediconnect.repository;

import com.mediconnect.model.EmailVerificationToken;
import com.mediconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findTopByUserOrderByCreatedAtDesc(User user);
}
