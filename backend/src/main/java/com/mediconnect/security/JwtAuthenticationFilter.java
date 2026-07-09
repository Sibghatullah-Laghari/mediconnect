package com.mediconnect.security;

import com.mediconnect.exception.AccountLockedException;
import com.mediconnect.model.User;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.service.AccountLockoutService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts HTTP requests to perform JWT-based authentication.
 * <p>
 * This filter runs once per request and validates the JWT token from the Authorization header.
 * If the token is valid, it loads the user details, checks account lockout status and email
 * verification, and sets the authentication in the SecurityContext. Public auth endpoints
 * are skipped to allow unauthenticated access.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;                             // Service for JWT operations
    private final CustomUserDetailsService userDetailsService;       // Service to load user details
    private final UserRepository userRepository;                     // Repository for user lookups
    private final AccountLockoutService accountLockoutService;       // Service to check account lockout

    /**
     * Performs JWT authentication for incoming requests.
     * <p>
     * Steps:
     * <ul>
     *   <li>Bypass authentication for public auth endpoints (register, login, verify, OTP).</li>
     *   <li>Extract the Bearer token from the Authorization header.</li>
     *   <li>Validate the token, extract claims, and verify token type is "access".</li>
     *   <li>Load user details from the database.</li>
     *   <li>Check if the user exists, is not locked out, and has verified email.</li>
     *   <li>If all checks pass, set the authentication in the SecurityContext.</li>
     *   <li>If any check fails, log the issue and continue without authentication.</li>
     * </ul>
     * </p>
     *
     * @param request  the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to continue
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip token validation for public auth endpoints (they do not require authentication)
        String servletPath = request.getServletPath();
        if (servletPath.startsWith("/auth/register") || servletPath.startsWith("/auth/login") ||
                servletPath.startsWith("/auth/verify") || servletPath.startsWith("/auth/send-otp") ||
                servletPath.startsWith("/auth/verify-otp")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract the Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);                       // Remove "Bearer " prefix
        try {
            Claims claims = jwtService.extractAllClaims(token);      // Validate and parse token
            String username = claims.getSubject();                    // Extract username (email)

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Ensure this is an access token, not a refresh token
                if (!"access".equals(claims.get("tokenType"))) {
                    log.warn("Invalid token type: {}", claims.get("tokenType"));
                    filterChain.doFilter(request, response);
                    return;
                }

                // Load user details from the database (cached by UserDetailsService)
                AuthenticatedUser userDetails = (AuthenticatedUser) userDetailsService.loadUserByUsername(username);

                // Verify that the token's subject matches the loaded user and token is valid
                if (username.equals(userDetails.getUsername())) {
                    User user = userRepository.findByEmail(username).orElse(null);
                    if (user == null) {
                        log.warn("User not found: {}", username);
                        filterChain.doFilter(request, response);
                        return;
                    }

                    // Check if the account is locked (throws AccountLockedException if locked)
                    try {
                        accountLockoutService.checkLockout(user);
                    } catch (AccountLockedException ex) {
                        log.warn("Account locked for user: {}", username);
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpStatus.FORBIDDEN.value());
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"" + ex.getMessage().replace("\"", "\\\"") + "\"}");
                        return;
                    }

                    // Ensure the user's email is verified
                    if (!user.isEmailVerified()) {
                        log.warn("Email not verified for user: {}", username);
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        return;
                    }

                    // Create an authentication token and set it in the SecurityContext
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    log.debug("User authenticated: {}", username);
                }
            }
        } catch (JwtException | IllegalArgumentException ex) {
            // Token is invalid, expired, or malformed – log and proceed without authentication
            log.error("JWT authentication failed: {}", ex.getMessage());
        }

        // Continue the filter chain
        filterChain.doFilter(request, response);
    }
}