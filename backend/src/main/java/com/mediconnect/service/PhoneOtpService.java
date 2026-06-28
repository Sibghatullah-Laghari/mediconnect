package com.mediconnect.service;

public interface PhoneOtpService {
    void generateAndSendOtp(String phone);
    boolean validateOtp(String phone, String otp);
}