package com.mediconnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Configuration class for time-related beans.
 * <p>
 * Provides a centralized Clock bean that can be injected throughout the application
 * for consistent time handling, especially useful for testing (e.g., fixed clocks)
 * and for operations requiring UTC timestamps.
 * </p>
 */
@Configuration
public class TimeConfig {

    /**
     * Provides the system's default clock with UTC time zone.
     * <p>
     * Using a Clock bean allows for easy substitution during testing
     * (e.g., with Clock.fixed()) and ensures all components use the same
     * time source for generating timestamps, token expirations, and logging.
     * </p>
     *
     * @return a Clock that returns the current instant in UTC
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
