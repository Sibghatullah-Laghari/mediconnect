package com.mediconnect.service;

import com.mediconnect.dto.auth.AuthResponse;
import com.mediconnect.dto.auth.LoginRequest;
import com.mediconnect.dto.auth.RefreshTokenRequest;
import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.dto.auth.UserResponse;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.EmailVerificationToken;
import com.mediconnect.model.RefreshToken;
import com.mediconnect.model.User;
import com.mediconnect.model.VerificationToken;
import com.mediconnect.repository.EmailVerificationTokenRepository;
import com.mediconnect.repository.RefreshTokenRepository;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.repository.VerificationTokenRepository;
import com.mediconnect.security.AuthenticatedUser;
import com.mediconnect.security.CustomUserDetailsService;
import com.mediconnect.security.JwtService;
import com.mediconnect.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Implementation of the AuthService interface for authentication and authorization operations.
 * <p>
 * This service handles user registration, login, token refresh, OTP verification,
 * phone verification, logout, and re‑sending verification emails. It integrates with
 * JWT, refresh tokens, account lockout, and email/phone OTP services.
 * </p>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AuthServiceImpl implements AuthService {

    private static final Duration OTP_EXPIRY = Duration.ofMinutes(10);      // Validity period for OTP tokens
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();    // Secure random generator for OTPs

    @Autowired
    @Lazy
    private EmailService emailService;                                      // Email service (injected lazily to avoid circular dependency)

    private final VerificationTokenRepository verificationTokenRepository;  // Repository for verification tokens (UUID)
    private final UserRepository userRepository;                            // Repository for users
    private final UserService userService;                                  // User service for registration logic
    private final RefreshTokenRepository refreshTokenRepository;            // Repository for refresh tokens
    private final EmailVerificationTokenRepository emailVerificationTokenRepository; // Repository for OTP tokens
    private final CustomUserDetailsService userDetailsService;              // Custom user details service for Spring Security
    private final JwtService jwtService;                                   // JWT generation and validation service
    private final Clock clock;                                             // Clock for timestamp operations
    private final AccountLockoutService accountLockoutService;             // Service for account lockout management
    private final PasswordEncoder passwordEncoder;                         // Password encoder (BCrypt)
    private final PhoneOtpServiceImpl phoneOtpService;                     // Service for phone OTP generation/validation

    @Value("${jwt.refresh-token.expiry.days}")
    private long refreshTokenExpiryDays;                                   // Refresh token expiry in days

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;                                                // Base URL for verification links

    /**
     * Registers a new user.
     * <p>
     * Delegates to UserService for registration, then builds and returns an authentication response
     * containing access and refresh tokens.
     * </p>
     *
     * @param request the registration request containing user details
     * @return AuthResponse containing tokens and user data
     * @throws ResourceNotFoundException if the user cannot be retrieved after registration
     */
    @Override
    public AuthResponse register(RegisterUserRequest request) {
        log.info("Registering user: {}", request.email());
        userService.registerUser(request);
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.email()));
        return buildAuthResponse(user);
    }

    /**
     * Authenticates a user with email and password.
     * <p>
     * Delegates to the authenticate method for credential validation, then builds
     * an authentication response with tokens. Lockout checks and failed attempt tracking
     * are handled inside authenticate.
     * </p>
     *
     * @param request the login request containing email and password
     * @return AuthResponse containing access and refresh tokens
     * @throws UnauthorizedException if credentials are invalid, account is locked, or email not verified
     */
    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.email());
        authenticate(request.email(), request.password());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        log.info("User logged in successfully: {}", request.email());
        return buildAuthResponse(user);
    }

    /**
     * Refreshes the access token using a valid refresh token.
     * <p>
     * The provided refresh token is hashed and looked up. If valid and not expired/revoked,
     * it is revoked and a new pair of tokens is generated for the associated user.
     * </p>
     *
     * @param request the refresh token request
     * @return new AuthResponse with fresh tokens
     * @throws UnauthorizedException if the refresh token is invalid, expired, or revoked
     */
    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.refreshToken());
        LocalDateTime now = LocalDateTime.now(clock);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid or expired"));

        if (storedToken.getRevokedAt() != null || storedToken.getExpiresAt().isBefore(now)) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        // Revoke the old token
        storedToken.setRevokedAt(now);
        refreshTokenRepository.save(storedToken);

        return buildAuthResponse(storedToken.getUser());
    }

    /**
     * Retrieves the currently authenticated user's profile.
     *
     * @return UserResponse containing user details
     * @throws UnauthorizedException if the current user cannot be found
     */
    @Override
    public UserResponse getCurrentUser() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UnauthorizedException("Current user not found"));

        return toResponse(user);
    }

    /**
     * Sends a 6-digit OTP to the user's email address for verification.
     * <p>
     * The OTP is generated, stored with a 10‑minute expiry, and sent via email.
     * </p>
     *
     * @param email the email address of the user
     * @throws ResourceNotFoundException if the user is not found
     */
    @Override
    public void sendOTP(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setOtp(otp);
        token.setExpiresAt(LocalDateTime.now(clock).plus(OTP_EXPIRY));
        token.setVerified(false);
        emailVerificationTokenRepository.save(token);

        emailService.sendEmail(email, "MediConnect verification code", "Your MediConnect OTP is " + otp + ". It expires in 10 minutes.");
    }

    /**
     * Verifies the OTP sent to the user's email.
     * <p>
     * Looks up the most recent unexpired, unverified OTP for the user. If valid,
     * the user's email is marked as verified and an authentication response is returned.
     * </p>
     *
     * @param email the user's email
     * @param otp   the OTP to verify
     * @return AuthResponse with tokens for the now-verified user
     * @throws UnauthorizedException if OTP is invalid or expired
     */
    @Override
    public AuthResponse verifyOTP(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        EmailVerificationToken token = emailVerificationTokenRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .filter(candidate -> !candidate.isExpired() && !candidate.isVerified() && candidate.getOtp().equals(otp))
                .orElseThrow(() -> new UnauthorizedException("Invalid OTP"));

        token.setVerified(true);
        emailVerificationTokenRepository.save(token);

        user.setEmailVerified(true);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    /**
     * Authenticates a user by email and password with lockout handling.
     * <p>
     * Steps:
     * <ul>
     *   <li>Retrieves the user; throws if not found</li>
     *   <li>Checks if the account is locked (via AccountLockoutService)</li>
     *   <li>Ensures email is verified</li>
     *   <li>Validates the password; if fails, records the failed attempt and locks if threshold reached</li>
     *   <li>On success, resets failed attempt counter</li>
     * </ul>
     * </p>
     *
     * @param email    the user's email
     * @param password the plaintext password
     * @throws UnauthorizedException if authentication fails for any reason
     */
    @Override
    public void authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Authentication failed: User not found with email: {}", email);
                    return new UnauthorizedException("Invalid email or password");
                });

        accountLockoutService.checkLockout(user);

        if (!user.isEmailVerified()) {
            log.warn("Authentication failed: Email not verified for user: {}", email);
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            log.warn("Authentication failed: Invalid password for user: {}", email);
            accountLockoutService.recordFailedAttempt(user);
            userRepository.save(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        // Successful login – reset lockout
        accountLockoutService.resetFailedAttempts(user);
        userRepository.save(user);
    }

    /**
     * Logs out the current user by revoking all their refresh tokens.
     *
     * @throws UnauthorizedException if the current user cannot be found
     */
    @Override
    public void logout() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UnauthorizedException("Current user not found"));

        refreshTokenRepository.revokeAllTokensByUser(user, LocalDateTime.now(clock));
    }

    /**
     * Resends the email verification link (UUID-based) to the user.
     * <p>
     * Invalidates any existing verification tokens for the user, generates a new token
     * with a 24‑hour expiry, and sends the verification link via email.
     * </p>
     *
     * @param email the user's email
     * @throws ResourceNotFoundException if the user is not found
     * @throws BadRequestException       if the email is already verified
     */
    @Override
    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        // Invalidate any existing verification token
        verificationTokenRepository.deleteByUser(user);
        emailVerificationTokenRepository.deleteByUser(user);

        // Generate new verification token (UUID)
        String token = UUID.randomUUID().toString();
        VerificationToken vt = new VerificationToken();
        vt.setToken(token);
        vt.setUser(user);
        vt.setExpiryDate(LocalDateTime.now(clock).plusHours(24));
        verificationTokenRepository.save(vt);

        // Send email with link
        String link = baseUrl + "/api/v1/auth/verify?token=" + token;
        emailService.sendEmail(email, "Verify your email", "Click: " + link);
    }

    /**
     * Sends an OTP to the user's phone number.
     * <p>
     * Updates the user's phone number in the database and triggers phone OTP generation.
     * </p>
     *
     * @param email the user's email
     * @param phone the phone number to send OTP to
     * @throws ResourceNotFoundException if the user is not found
     */
    @Override
    public void sendPhoneOtp(String email, String phone) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPhone(phone);
        userRepository.save(user);
        phoneOtpService.generateAndSendOtp(phone);
    }

    /**
     * Verifies the phone OTP for a user.
     * <p>
     * Ensures the provided phone matches the stored phone for the user, then validates
     * the OTP via PhoneOtpService. If valid, marks the user's phone as verified.
     * </p>
     *
     * @param email the user's email
     * @param phone the phone number
     * @param otp   the OTP to verify
     * @throws BadRequestException if phone mismatch or OTP invalid/expired
     * @throws ResourceNotFoundException if the user is not found
     */
    @Override
    public void verifyPhoneOtp(String email, String phone, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!user.getPhone().equals(phone)) {
            throw new BadRequestException("Phone number does not match");
        }
        if (phoneOtpService.validateOtp(phone, otp)) {
            user.setPhoneVerified(true);
            userRepository.save(user);
        } else {
            throw new BadRequestException("Invalid or expired OTP");
        }
    }

    // ----- PRIVATE HELPER METHODS -----

    /**
     * Builds an AuthResponse containing access token, refresh token, and user details.
     *
     * @param user the authenticated user
     * @return AuthResponse with tokens and user data
     */
    private AuthResponse buildAuthResponse(User user) {
        AuthenticatedUser userDetails = (AuthenticatedUser) userDetailsService.loadUserByUsername(user.getEmail());

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = createRefreshToken(user);
        return new AuthResponse(
                accessToken,
                refreshToken,
                toResponse(user)
        );
    }

    /**
     * Creates a new refresh token for a user.
     * <p>
     * Generates a raw token, hashes it for storage, sets an expiry date, and persists it.
     * </p>
     *
     * @param user the user for whom the token is created
     * @return the raw (unhashed) refresh token string to be returned to the client
     */
    private String createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        String rawToken = generateRefreshTokenValue();
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpiresAt(LocalDateTime.now(clock).plusDays(refreshTokenExpiryDays));
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }

    /**
     * Generates a secure random string for use as a raw refresh token.
     *
     * @return Base64-encoded random string (without padding)
     */
    private String generateRefreshTokenValue() {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes());
    }

    /**
     * Computes the SHA-256 hash of a given token string.
     *
     * @param token the raw token string
     * @return hexadecimal representation of the SHA-256 hash
     * @throws IllegalStateException if SHA-256 algorithm is not available
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Converts a User entity to a UserResponse DTO.
     *
     * @param user the user entity
     * @return UserResponse with relevant user fields
     */
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isEmailVerified()
        );
    }
}