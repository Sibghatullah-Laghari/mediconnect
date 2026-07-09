package com.mediconnect.service;

import com.mediconnect.exception.AccountLockedException;
import com.mediconnect.model.User;
import com.mediconnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Service responsible for managing account lockout logic based on failed login attempts.
 * <p>
 * This service tracks the number of consecutive failed login attempts per user.
 * If the number exceeds a configurable threshold, the account is locked for a specified duration.
 * It also handles auto-unlock when the lockout period expires.
 * </p>
 */
@Service
@RequiredArgsConstructor
public class AccountLockoutService {

    @Value("${app.security.max-failed-attempts:5}")          // Maximum allowed failed attempts before lockout
    private int maxFailedAttempts;

    @Value("${app.security.lockout-duration-minutes:15}")    // Duration in minutes for which the account is locked
    private int lockoutDurationMinutes;

    private final UserRepository userRepository;              // Repository for persisting user lockout data
    private final Clock clock;                                // Clock for time-based operations

    /**
     * Checks if a user's account is currently locked.
     * <p>
     * If the account is locked and the lockout time has passed, the lock is automatically cleared.
     * If still locked, an exception is thrown.
     * </p>
     *
     * @param user the user to check for lockout
     * @throws AccountLockedException if the account is still locked
     */
    public void checkLockout(User user) {
        if (user.getLockedUntil() != null) {
            // If lockout time has passed, clear the lock
            if (user.getLockedUntil().isAfter(LocalDateTime.now(clock))) {
                throw new AccountLockedException(
                        "Account is locked until " + user.getLockedUntil() + ". Please try again later."
                );
            }
            // Auto-unlock if lockout period has expired
            resetFailedAttempts(user);
        }
    }

    /**
     * Records a failed login attempt for a user.
     * <p>
     * Increments the failed attempt counter. If the counter reaches the maximum allowed,
     * the account is locked until the configured lockout duration elapses.
     * The updated user entity is persisted.
     * </p>
     *
     * @param user the user for whom a failed attempt is recorded
     */
    @Transactional
    public void recordFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        // Lock the account if attempts exceed the threshold
        if (attempts >= maxFailedAttempts) {
            user.setLockedUntil(LocalDateTime.now(clock).plusMinutes(lockoutDurationMinutes));
        }

        userRepository.save(user);
    }

    /**
     * Resets failed login attempts and clears any lockout for a user.
     * <p>
     * This is typically called after a successful login or when a lockout expires.
     * The updated user entity is persisted.
     * </p>
     *
     * @param user the user whose failed attempts and lockout should be reset
     */
    @Transactional
    public void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }
}
