package com.mediconnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, RateLimitInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String clientIp = request.getRemoteAddr();
        String endpoint = request.getRequestURI();
        String key = clientIp + ":" + endpoint;

        RateLimitInfo info = requestCounts.computeIfAbsent(key, k -> new RateLimitInfo());

        if (info.isLimitExceeded()) {
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded. Max 100 requests per minute.");
            return;
        }

        info.recordRequest();
        filterChain.doFilter(request, response);
    }
}
