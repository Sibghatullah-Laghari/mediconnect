package com.mediconnect.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Filter responsible for rate limiting authentication endpoints.
 * <p>
 * Intercepts requests to sensitive authentication endpoints (login, register, OTP, refresh)
 * and applies a limit of 5 attempts per minute for each client IP using the Bucket4j library.
 * A 429 Too Many Requests response is returned when the configured limit is exceeded.
 * </p>
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    /**
     * In-memory store that maps client IP addresses to individual token buckets.
     * Each bucket tracks allowed requests within the configured time window.
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Applies rate limiting to authentication requests before continuing the filter chain.
     * <p>
     * The client IP is extracted by preferring the X-Forwarded-For header when available,
     * then a bucket is retrieved or created for that IP before consuming a token.
     * Successful requests continue through the chain; otherwise, a 429 response is returned.
     * </p>
     *
     * @param req   the HttpServletRequest
     * @param res   the HttpServletResponse
     * @param chain the FilterChain used to continue request processing
     * @throws ServletException if a servlet-related error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {
        // Apply rate limiting only to authentication endpoints
        if (req.getRequestURI().matches(".*/auth/(login|register|send-otp|verify-otp|refresh)")) {
            // Resolve the client IP from the proxy header or remote address
            String ip = req.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = req.getRemoteAddr();
            }

            // Find or initialize the token bucket associated with this IP
            Bucket bucket = buckets.computeIfAbsent(ip, key -> Bucket4j.builder()
                    .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                    .build());

            // Attempt to consume a single token from the current bucket
            if (!bucket.tryConsume(1)) {
                // Log the exceeded limit and return a 429 response
                log.warn("Rate limit exceeded for IP: {} at {}", ip, req.getRequestURI());
                res.setStatus(429);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Too many authentication attempts. Please try again later.\"}");
                return;
            }
        }

        // Pass the request to the remaining filters
        chain.doFilter(req, res);
    }
}
