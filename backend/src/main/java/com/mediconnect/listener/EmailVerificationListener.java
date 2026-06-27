package com.mediconnect.listener;

import com.mediconnect.event.UserRegisteredEvent;
import com.mediconnect.model.User;
import com.mediconnect.model.VerificationToken;
import com.mediconnect.repository.VerificationTokenRepository;
import com.mediconnect.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.time.Clock;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationListener {

    private final VerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final Clock clock;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegistered(UserRegisteredEvent event) {
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now(clock).plusHours(24);

        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(user);
        vt.setExpiryDate(expiry);
        tokenRepository.save(vt);

        String verificationLink = baseUrl + "/api/v1/auth/verify?token=" + token;
        String subject = "Welcome to MediConnect – Verify your email";
        String body = "Hi " + user.getName() + ",\n\nPlease click the link below to verify your email:\n" + verificationLink +
                "\n\nThis link expires in 24 hours.\n\nThank you,\nMediConnect Team";

        try {
            emailService.sendEmail(user.getEmail(), subject, body);
            log.info("Verification email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }
}