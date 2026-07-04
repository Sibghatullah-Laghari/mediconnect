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
 * Filter that applies rate limiting to authentication endpoints.
 * <p>
 * This filter intercepts requests to sensitive auth endpoints (login, register, OTP, refresh)
 * and enforces a limit of 5 attempts per minute per client IP address using the Bucket4j library.
 * When the limit is exceeded, a 429 Too Many Requests response is returned.
 * </p>
 */
@Slf4j
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    /**
     * In-memory cache mapping client IP addresses to their respective token buckets.
     * Each bucket tracks the number of allowed requests within a time window.
     */
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Performs rate limiting for authentication requests before passing the request down the filter chain.
     * <p>
     * The method extracts the client IP (preferring X-Forwarded-For header for proxies),
     * retrieves or creates a bucket for that IP, and attempts to consume one token.
     * If successful, the request proceeds; otherwise, a 429 response is sent.
     * </p>
     *
     * @param req  the HttpServletRequest
     * @param res  the HttpServletResponse
     * @param chain the FilterChain to continue the request processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {
        // Only apply rate limiting to authentication-related endpoints
        if (req.getRequestURI().matches(".*/auth/(login|register|send-otp|verify-otp|refresh)")) {
            // Determine client IP: use X-Forwarded-For if available, otherwise fallback to remote address
            String ip = req.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) {
                ip = req.getRemoteAddr();
            }

            // Retrieve or create a token bucket for this IP
            Bucket bucket = buckets.computeIfAbsent(ip, key -> Bucket4j.builder()
                    .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
                    .build());

            // Try to consume one token from the bucket
            if (!bucket.tryConsume(1)) {
                // Rate limit exceeded – log and return 429 Too Many Requests
                log.warn("Rate limit exceeded for IP: {} at {}", ip, req.getRequestURI());
                res.setStatus(429);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Too many authentication attempts. Please try again later.\"}");
                return;
            }
        }

        // Continue with the rest of the filter chain
        chain.doFilter(req, res);
    }
}