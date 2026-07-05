package com.mediconnect.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service implementation for phone-based OTP (One-Time Password) operations.
 * <p>
 * This service generates, stores, and validates OTPs for phone number verification.
 * OTPs are stored in‑memory with a 5‑minute expiry. This implementation is a mock
 * that logs the OTP instead of sending an actual SMS.
 * </p>
 */
@Slf4j
@Service
public class PhoneOtpServiceImpl implements PhoneOtpService {

    private static final int OTP_LENGTH = 6;                                 // Length of the OTP code
    private static final int OTP_TTL_SECONDS = 300;                          // Time‑to‑live in seconds (5 minutes)

    /**
     * In‑memory store mapping phone numbers to their OTP entries (code + expiry).
     * Uses ConcurrentHashMap for thread‑safe operations.
     */
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private final SecureRandom random = new SecureRandom();                  // Secure random generator for OTPs

    /**
     * Generates a 6‑digit OTP for the given phone number, stores it with a 5‑minute expiry,
     * and logs it (mock SMS). In a production environment, this would send an actual SMS.
     *
     * @param phone the phone number to send the OTP to
     */
    @Override
    public void generateAndSendOtp(String phone) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        otpStore.put(phone, new OtpEntry(otp, Instant.now().plusSeconds(OTP_TTL_SECONDS)));
        // TODO: replace with actual SMS integration
        log.info("📱 Mock SMS: OTP for {} is {}", phone, otp);
    }

    /**
     * Validates the provided OTP for the given phone number.
     * <p>
     * If the OTP matches and is not expired, it is removed from the store
     * (one‑time use) and true is returned. Otherwise, false is returned.
     * Expired entries are automatically purged during validation.
     * </p>
     *
     * @param phone the phone number associated with the OTP
     * @param otp   the OTP code to validate
     * @return true if the OTP is valid and not expired, false otherwise
     */
    @Override
    public boolean validateOtp(String phone, String otp) {
        OtpEntry entry = otpStore.get(phone);
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiry)) {
            otpStore.remove(phone);
            return false;
        }
        boolean valid = entry.otp.equals(otp);
        if (valid) otpStore.remove(phone);                                   // OTP is consumed after successful validation
        return valid;
    }

    /**
     * Internal record to hold an OTP value and its expiration timestamp.
     *
     * @param otp    the OTP code
     * @param expiry the instant when the OTP expires
     */
    private record OtpEntry(String otp, Instant expiry) {}
}