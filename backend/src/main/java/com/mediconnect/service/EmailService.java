package com.mediconnect.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    public void sendEmail(String to, String subject, String body) {
        log.info("Sending email to: {}, Subject: {}, Body: {}", to, subject, body);
        // Placeholder for actual email sending logic
    }
}
