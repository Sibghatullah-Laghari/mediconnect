package com.mediconnect.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class PhoneOtpServiceImpl implements PhoneOtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_TTL_SECONDS = 300; // 5 minutes
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    @Override
    public void generateAndSendOtp(String phone) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        otpStore.put(phone, new OtpEntry(otp, Instant.now().plusSeconds(OTP_TTL_SECONDS)));
        // TODO: replace with actual SMS integration
        log.info("📱 Mock SMS: OTP for {} is {}", phone, otp);
    }

    @Override
    public boolean validateOtp(String phone, String otp) {
        OtpEntry entry = otpStore.get(phone);
        if (entry == null) return false;
        if (Instant.now().isAfter(entry.expiry)) {
            otpStore.remove(phone);
            return false;
        }
        boolean valid = entry.otp.equals(otp);
        if (valid) otpStore.remove(phone);
        return valid;
    }

    private record OtpEntry(String otp, Instant expiry) {}
}