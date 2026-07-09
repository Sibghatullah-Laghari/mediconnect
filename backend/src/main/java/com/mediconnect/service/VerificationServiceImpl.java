package com.mediconnect.service;

import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.User;
import com.mediconnect.model.VerificationToken;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

/**
 * Service implementation for email verification operations.
 * <p>
 * Handles the verification of user email addresses using a one‑time token.
 * Tokens are validated for existence, usage status, and expiry before
 * marking the associated user as email‑verified.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VerificationServiceImpl implements VerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    /**
     * Verifies a user's email address using a verification token.
     * <p>
     * The token is looked up by its string value. If found, the method checks:
     * <ul>
     *   <li>If the token has already been used → throws UnauthorizedException</li>
     *   <li>If the token has expired (based on the system clock) → throws UnauthorizedException</li>
     *   <li>If the associated user's email is already verified → throws UnauthorizedException</li>
     * </ul>
     * If all checks pass, the user's emailVerified flag is set to true,
     * the user is persisted, and the token is marked as used.
     * </p>
     *
     * @param token the verification token string
     * @throws ResourceNotFoundException if the token does not exist
     * @throws UnauthorizedException if the token is used, expired, or email already verified
     */
    @Override
    public void verifyEmail(String token) {
        // Retrieve the verification token or throw if not found
        VerificationToken vt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired verification token"));

        // Prevent reuse of the same token
        if (vt.isUsed()) {
            throw new UnauthorizedException("Token already used");
        }

        // Check if the token has expired against the current time
        if (vt.getExpiryDate().isBefore(LocalDateTime.now(clock))) {
            throw new UnauthorizedException("Token expired");
        }

        // Get the associated user
        User user = vt.getUser();
        // Avoid redundant verification
        if (user.isEmailVerified()) {
            throw new UnauthorizedException("Email already verified");
        }

        // Mark email as verified
        user.setEmailVerified(true);
        userRepository.save(user);

        // Mark the token as used to prevent replay
        tokenRepository.markAsUsed(token);

        log.info("Email verified for user: {}", user.getEmail());
    }
}