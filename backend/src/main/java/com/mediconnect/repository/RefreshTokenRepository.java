package com.mediconnect.repository;

import com.mediconnect.model.RefreshToken;
import com.mediconnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.tokenHash = :hash")
    Optional<RefreshToken> findByTokenHashForUpdate(@Param("hash") String tokenHash);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt WHERE rt.tokenHash = :hash AND rt.revokedAt IS NULL AND rt.expiresAt > :now")
    int revokeToken(@Param("hash") String tokenHash, @Param("revokedAt") LocalDateTime revokedAt, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :now WHERE rt.user = :user AND rt.revokedAt IS NULL")
    int revokeAllTokensByUser(@Param("user") User user, @Param("now") LocalDateTime now);

    long countByTokenHashAndRevokedAtIsNullAndExpiresAtAfter(String tokenHash, LocalDateTime now);

    long deleteByExpiresAtBefore(LocalDateTime expiresAt);

    long deleteByUserAndRevokedAtIsNotNull(User user);
}
