package com.mediconnect.service;

public interface EmailService {
    void sendVerificationEmail(String to, String code);
    void sendPasswordResetEmail(String to, String resetToken);
}
