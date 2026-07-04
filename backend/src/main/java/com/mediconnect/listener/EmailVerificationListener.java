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

/**
 * Event listener that handles user registration events by creating a verification token
 * and sending a verification email to the newly registered user.
 * <p>
 * This listener is triggered asynchronously after the user registration transaction
 * commits successfully, ensuring that the email sending does not block the main flow.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EmailVerificationListener {

    private final VerificationTokenRepository tokenRepository;  // Repository for storing verification tokens
    private final EmailService emailService;                   // Service for sending emails
    private final Clock clock;                                 // Clock for generating expiry timestamps

    @Value("${app.base-url:http://localhost:8080}")            // Base URL for constructing verification links
    private String baseUrl;

    /**
     * Handles the UserRegisteredEvent after the transaction is committed.
     * <p>
     * This method generates a unique verification token, persists it with a 24-hour expiry,
     * constructs a verification link, and sends an email to the user's registered address.
     * The method is asynchronous to avoid delaying the registration response.
     * </p>
     *
     * @param event the UserRegisteredEvent containing the newly registered user
     */
    @Async                                                  // Executes asynchronously in a separate thread
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)  // Triggers only after successful commit
    public void handleUserRegistered(UserRegisteredEvent event) {
        User user = event.getUser();

        // Generate a random UUID as the verification token
        String token = UUID.randomUUID().toString();

        // Set expiry to 24 hours from current time using the injected Clock
        LocalDateTime expiry = LocalDateTime.now(clock).plusHours(24);

        // Create and save the verification token entity
        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(user);
        vt.setExpiryDate(expiry);
        tokenRepository.save(vt);

        // Construct the verification link
        String verificationLink = baseUrl + "/api/v1/auth/verify?token=" + token;

        // Prepare email subject and body
        String subject = "Welcome to MediConnect – Verify your email";
        String body = "Hi " + user.getName() + ",\n\nPlease click the link below to verify your email:\n" + verificationLink +
                "\n\nThis link expires in 24 hours.\n\nThank you,\nMediConnect Team";

        // Attempt to send the email and log the outcome
        try {
            emailService.sendEmail(user.getEmail(), subject, body);
            log.info("Verification email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", user.getEmail(), e.getMessage(), e);
        }
    }
}