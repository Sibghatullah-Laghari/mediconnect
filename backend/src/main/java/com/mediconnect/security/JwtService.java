package com.mediconnect.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";

    @Value("${jwt.secret:this-is-my-super-secret-development-key-32-chars}")
    private String jwtSecret;

    @Value("${jwt.expiry.minutes:60}")
    private long accessTokenExpiryMinutes;

    @PostConstruct
    void validateSecret() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured");
        }

        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes long");
        }
    }

    public String generateAccessToken(AuthenticatedUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, "access");
        claims.put("userId", user.getId());
        claims.put("verified", user.isEmailVerified());
        claims.put(
                "role",
                user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .findFirst()
                        .orElse("ROLE_PATIENT")
        );
        return buildToken(claims, user, Duration.ofMinutes(accessTokenExpiryMinutes).toMillis());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return Objects.equals(extractUsername(token), userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class)));
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class)));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private String buildToken(Map<String, Object> claims, UserDetails userDetails, long expirationMs) {
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
