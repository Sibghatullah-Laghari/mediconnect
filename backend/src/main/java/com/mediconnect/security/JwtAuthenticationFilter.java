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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final AccountLockoutService accountLockoutService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Skip token validation for public auth endpoints
        String servletPath = request.getServletPath();
        if (servletPath.startsWith("/auth/register") || servletPath.startsWith("/auth/login") ||
                servletPath.startsWith("/auth/verify") || servletPath.startsWith("/auth/send-otp") ||
                servletPath.startsWith("/auth/verify-otp")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtService.extractAllClaims(token);
            String username = claims.getSubject();

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (!"access".equals(claims.get("tokenType"))) {
                    log.warn("Invalid token type: {}", claims.get("tokenType"));
                    filterChain.doFilter(request, response);
                    return;
                }

                // Optimization: Load user details once. This hits DB.
                AuthenticatedUser userDetails = (AuthenticatedUser) userDetailsService.loadUserByUsername(username);

                // We still check valid token (subject matches and not expired)
                // Note: extractAllClaims already checked expiration
                if (username.equals(userDetails.getUsername())) {
                    User user = userRepository.findByEmail(username).orElse(null);
                    if (user == null) {
                        log.warn("User not found: {}", username);
                        filterChain.doFilter(request, response);
                        return;
                    }

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

                    if (!user.isEmailVerified()) {
                        log.warn("Email not verified for user: {}", username);
                        SecurityContextHolder.clearContext();
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        response.setContentType("application/json");
                        response.getWriter().write("{\"error\":\"Unauthorized\"}");
                        return;
                    }

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
            log.error("JWT authentication failed: {}", ex.getMessage());
            // Token is invalid or expired
        }

        filterChain.doFilter(request, response);
    }
}