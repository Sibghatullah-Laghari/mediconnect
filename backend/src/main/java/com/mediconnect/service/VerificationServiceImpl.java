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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class VerificationServiceImpl implements VerificationService {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    public void verifyEmail(String token) {
        VerificationToken vt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired verification token"));

        if (vt.isUsed()) {
            throw new UnauthorizedException("Token already used");
        }

        if (vt.getExpiryDate().isBefore(LocalDateTime.now(clock))) {
            throw new UnauthorizedException("Token expired");
        }

        User user = vt.getUser();
        if (user.isEmailVerified()) {
            throw new UnauthorizedException("Email already verified");
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        tokenRepository.markAsUsed(token);

        log.info("Email verified for user: {}", user.getEmail());
    }
}