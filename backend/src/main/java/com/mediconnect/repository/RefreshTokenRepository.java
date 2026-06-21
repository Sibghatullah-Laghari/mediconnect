package com.mediconnect.repository;

import com.mediconnect.model.RefreshToken;
import com.mediconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    long deleteByExpiresAtBefore(LocalDateTime expiresAt);

    long deleteByUserAndRevokedAtIsNotNull(User user);
}
