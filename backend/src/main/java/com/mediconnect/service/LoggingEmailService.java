package com.mediconnect.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Profile({"dev", "test", "default"})
public class LoggingEmailService implements EmailService {

    @Override
    public void sendVerificationEmail(String to, String code) {
        log.info("DEV EMAIL — verification for {}: code={}", to, code);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        log.info("DEV EMAIL — password reset for {}: token={}", to, resetToken);
    }
}
