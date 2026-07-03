package com.mediconnect.config;

import com.mediconnect.filter.RateLimitingFilter;
import com.mediconnect.filter.SecurityHeadersFilter;
import com.mediconnect.security.JwtAuthenticationFilter;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration class for security settings of the application.
 * <p>
 * This class centralizes all security-related beans and configurations including
 * password encoding, JWT authentication, rate limiting, security headers,
 * CORS, and HTTP security filter chain setup. It also enables method-level security,
 * asynchronous processing, and scheduled tasks.
 * </p>
 */
@Slf4j
@Configuration
@EnableMethodSecurity          // Enables @PreAuthorize, @PostAuthorize etc. for method security
@EnableAsync                    // Enables asynchronous method execution using @Async
@EnableScheduling               // Enables scheduled tasks using @Scheduled
@RequiredArgsConstructor        // Generates constructor injection for final fields
public class SecurityConfig {

    // Injected filters for JWT authentication, rate limiting, and security headers
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final SecurityHeadersFilter securityHeadersFilter;

    @Value("${app.cors.allowed-origins}")   // Comma-separated list of allowed origins from properties
    private String allowedOrigins;

    /**
     * Provides a BCrypt-based password encoder for hashing and verifying passwords.
     * <p>
     * BCrypt is a strong, adaptive hashing function that incorporates a salt and
     * configurable work factor to resist brute-force attacks.
     * </p>
     *
     * @return PasswordEncoder instance using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures the Spring Security filter chain for the application.
     * <p>
     * This method sets up a stateless, JWT-based security context by:
     * <ul>
     *   <li>Disabling CSRF protection (stateless APIs do not require it)</li>
     *   <li>Enabling CORS with custom configuration</li>
     *   <li>Using session creation policy STATELESS (no sessions)</li>
     *   <li>Defining public endpoints (permitAll) and protected endpoints (authenticated)</li>
     *   <li>Adding custom filters in the correct order:
     *       <ol>
     *         <li>SecurityHeadersFilter – adds security headers to responses</li>
     *         <li>RateLimitingFilter – applies rate limiting per client</li>
     *         <li>JwtAuthenticationFilter – validates JWT and sets authentication</li>
     *       </ol>
     *   </li>
     * </ul>
     *
     * @param http the HttpSecurity object to configure
     * @return the built SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Log the allowed origins for debugging purposes
        log.info("======================================");
        log.info("Allowed origins property: {}", allowedOrigins);
        log.info("======================================");

        http
                // Disable CSRF because we use stateless JWT tokens
                .csrf(csrf -> csrf.disable())

                // Apply custom CORS configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Use stateless session management (no HTTP sessions)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Define authorization rules for incoming requests
                .authorizeHttpRequests(auth -> auth
                        // Endpoints that require authentication
                        .requestMatchers("/auth/me", "/doctors/me", "/patients/me").authenticated()

                        // Public endpoints that are accessible without authentication
                        .requestMatchers(
                                "/auth/register",
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/send-otp",
                                "/auth/verify-otp",
                                "/auth/logout",
                                "/users/register",
                                "/actuator/health",
                                "/auth/verify",
                                "/auth/send-phone-otp",
                                "/auth/verify-phone"
                        ).permitAll()

                        // Public GET endpoints for doctors and specializations
                        .requestMatchers(HttpMethod.GET,
                                "/doctors",
                                "/doctors/*",
                                "/doctors/specializations",
                                "/doctors/specialization/**")
                        .permitAll()

                        // Allow preflight OPTIONS requests for CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // All other requests must be authenticated
                        .anyRequest().authenticated()
                )

                // Add custom filters before the default UsernamePasswordAuthenticationFilter
                .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Provides a CORS configuration source that defines allowed origins, methods,
     * headers, and credentials for cross-origin requests.
     * <p>
     * The allowed origins are parsed from a comma-separated property and validated:
     * <ul>
     *   <li>Wildcard origins ("*") are rejected</li>
     *   <li>At least one origin must be provided</li>
     * </ul>
     * </p>
     *
     * @return CorsConfigurationSource configured for the entire application
     * @throws IllegalStateException if no valid origins are configured or a wildcard is used
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        // Parse the comma-separated origins, trim whitespace, and filter empty strings
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .peek(origin -> {
                    // Reject wildcard origins for security reasons
                    if ("*".equals(origin)) {
                        throw new IllegalStateException("Wildcard CORS origins are not allowed");
                    }
                })
                .toList();

        // Ensure at least one valid origin is configured
        if (origins.isEmpty()) {
            throw new IllegalStateException("At least one allowed origin must be configured");
        }

        log.info("Parsed allowed origins: {}", origins);

        // Build CORS configuration with the parsed origins
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Origin", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Authorization"));   // Expose Authorization header to client
        configuration.setAllowCredentials(true);                     // Allow credentials (cookies, authorization headers)
        configuration.setMaxAge(3600L);                              // Cache preflight response for 1 hour

        // Register the configuration for all endpoints
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}