package com.mediconnect.security;

import java.time.LocalDateTime;

public class RateLimitInfo {
    private int count = 0;
    private LocalDateTime windowStart = LocalDateTime.now();

    public synchronized boolean isLimitExceeded() {
        if (LocalDateTime.now().isAfter(windowStart.plusMinutes(1))) {
            count = 0;
            windowStart = LocalDateTime.now();
        }
        return count >= 100;
    }

    public synchronized void recordRequest() {
        count++;
    }
}
