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

/**
 * Service for JWT (JSON Web Token) operations.
 * <p>
 * This service handles generation, validation, and extraction of access tokens.
 * It uses a configured secret key (minimum 32 bytes) and supports claims for
 * token type ("access"), user ID, verification status, and role. Both access
 * and refresh tokens are supported through the tokenType claim.
 * </p>
 */
@Service
public class JwtService {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";      // Claim key for token type (access/refresh)

    @Value("${jwt.secret:this-is-my-super-secret-development-key-32-chars}")
    private String jwtSecret;                                        // Secret key for signing JWTs (must be ≥ 32 bytes)

    @Value("${jwt.expiry.minutes:60}")
    private long accessTokenExpiryMinutes;                           // Access token validity in minutes

    /**
     * Validates that the JWT secret is properly configured.
     * <p>
     * Ensures the secret is not null/blank and has a minimum length of 32 bytes
     * to meet the HS256 algorithm requirements.
     * </p>
     *
     * @throws IllegalStateException if the secret is missing or too short
     */
    @PostConstruct
    void validateSecret() {
        if (jwtSecret == null || jwtSecret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET must be configured");
        }

        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 bytes long");
        }
    }

    /**
     * Generates an access token for the given authenticated user.
     * <p>
     * The token includes claims for token type ("access"), user ID, email verification status,
     * and the user's role. The token is signed with the configured secret and expires
     * after the configured minutes.
     * </p>
     *
     * @param user the authenticated user details
     * @return a signed JWT access token string
     */
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

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token the JWT token
     * @return the username (email) stored in the subject claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Validates a token against the provided user details.
     * <p>
     * Checks that the token's subject matches the username and that the token is not expired.
     * </p>
     *
     * @param token the JWT token
     * @param userDetails the user details to validate against
     * @return true if the token is valid for the given user, false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return Objects.equals(extractUsername(token), userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Checks if the token is a refresh token based on the tokenType claim.
     *
     * @param token the JWT token
     * @return true if the tokenType claim is "refresh", false otherwise
     */
    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class)));
    }

    /**
     * Checks if the token is an access token based on the tokenType claim.
     *
     * @param token the JWT token
     * @return true if the tokenType claim is "access", false otherwise
     */
    public boolean isAccessToken(String token) {
        return "access".equals(extractClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class)));
    }

    /**
     * Extracts a specific claim from the token using a resolver function.
     *
     * @param token the JWT token
     * @param claimsResolver a function to extract the desired claim from the Claims object
     * @param <T> the type of the claim
     * @return the extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    /**
     * Builds a signed JWT token with the given claims, subject, and expiration.
     *
     * @param claims the claims to include in the token
     * @param userDetails the user details (for subject)
     * @param expirationMs the expiration time in milliseconds from now
     * @return the signed JWT string
     */
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

    /**
     * Checks if the token has expired.
     *
     * @param token the JWT token
     * @return true if the token's expiration date is before the current time, false otherwise
     */
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Extracts all claims from the JWT token.
     *
     * @param token the JWT token
     * @return the Claims object containing all claims
     * @throws io.jsonwebtoken.JwtException if the token is invalid or expired
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Creates a SecretKey from the configured jwtSecret using HMAC-SHA.
     *
     * @return the SecretKey for signing and verifying tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}