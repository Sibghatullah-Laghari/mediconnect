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

@Service
@RequiredArgsConstructor
public class AccountLockoutService {

    @Value("${app.security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.lockout-duration-minutes:15}")
    private int lockoutDurationMinutes;

    private final UserRepository userRepository;
    private final Clock clock;

    public void checkLockout(User user) {
        if (user.getLockedUntil() != null) {
            if (user.getLockedUntil().isAfter(LocalDateTime.now(clock))) {
                throw new AccountLockedException(
                        "Account is locked until " + user.getLockedUntil() + ". Please try again later."
                );
            }
            // Auto-unlock if lockout period has expired
            resetFailedAttempts(user);
        }
    }

    @Transactional
    public void recordFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxFailedAttempts) {
            user.setLockedUntil(LocalDateTime.now(clock).plusMinutes(lockoutDurationMinutes));
        }

        userRepository.save(user);
    }

    @Transactional
    public void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }
}