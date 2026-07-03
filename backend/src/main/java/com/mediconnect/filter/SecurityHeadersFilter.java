package com.mediconnect.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that adds security-related HTTP headers to every response.
 * <p>
 * This filter ensures that the application adheres to security best practices
 * by setting headers that mitigate common web vulnerabilities such as
 * MIME-sniffing, clickjacking, cross-origin resource sharing issues,
 * and data leakage through referrer or policy violations.
 * </p>
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    /**
     * Adds a set of security headers to the HTTP response.
     * <p>
     * The headers added include:
     * <ul>
     *   <li><b>X-Content-Type-Options: nosniff</b> – Prevents browsers from MIME-sniffing responses</li>
     *   <li><b>X-Frame-Options: DENY</b> – Prevents the page from being embedded in frames (clickjacking)</li>
     *   <li><b>Strict-Transport-Security</b> – Enforces HTTPS for 1 year, including subdomains</li>
     *   <li><b>Referrer-Policy</b> – Controls how much referrer info is sent with cross-origin requests</li>
     *   <li><b>Permissions-Policy</b> – Disables browser features (camera, microphone, geolocation)</li>
     *   <li><b>Content-Security-Policy</b> – Restricts resources to 'none' and disallows framing</li>
     * </ul>
     * </p>
     *
     * @param request  the HttpServletRequest
     * @param response the HttpServletResponse
     * @param filterChain the FilterChain to continue the request processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Prevents browser from interpreting files as a different MIME type
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Prevents the page from being displayed in a frame (protects against clickjacking)
        response.setHeader("X-Frame-Options", "DENY");

        // Enforces HTTPS for the site and all subdomains for 1 year
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Restricts the information sent in the Referer header for cross-origin requests
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Disables access to sensitive browser features for enhanced privacy
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");

        // Restricts content sources to 'none' and prevents the page from being framed by any ancestor
        response.setHeader("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; base-uri 'none'");

        // Proceed with the rest of the filter chain
        filterChain.doFilter(request, response);
    }
}