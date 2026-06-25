package com.mediconnect.service;

import com.mediconnect.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final Clock clock;

    /**
     * Prunes expired refresh tokens from the database.
     * Runs every day at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void pruneExpiredTokens() {
        log.info("Starting scheduled pruning of expired refresh tokens");
        long deletedCount = refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now(clock));
        log.info("Deleted {} expired refresh tokens", deletedCount);
    }
}
