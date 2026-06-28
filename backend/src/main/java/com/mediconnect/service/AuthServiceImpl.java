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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AuthServiceImpl implements AuthService {

    private static final Duration OTP_EXPIRY = Duration.ofMinutes(10);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Autowired
    @Lazy
    private EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final Clock clock;
    private final AccountLockoutService accountLockoutService;
    private final PasswordEncoder passwordEncoder;
    private final PhoneOtpServiceImpl phoneOtpService;


    @Value("${jwt.refresh-token.expiry.days}")
    private long refreshTokenExpiryDays;

    @Override
    public AuthResponse register(RegisterUserRequest request) {
        log.info("Registering user: {}", request.email());
        userService.registerUser(request);
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.email()));
        return buildAuthResponse(user);
    }

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.email());
        authenticate(request.email(), request.password());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        log.info("User logged in successfully: {}", request.email());
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String tokenHash = hashToken(request.refreshToken());
        LocalDateTime now = LocalDateTime.now(clock);

        RefreshToken storedToken = refreshTokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid or expired"));

        if (storedToken.getRevokedAt() != null || storedToken.getExpiresAt().isBefore(now)) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        storedToken.setRevokedAt(now);
        refreshTokenRepository.save(storedToken);

        return buildAuthResponse(storedToken.getUser());
    }

    @Override
    public UserResponse getCurrentUser() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UnauthorizedException("Current user not found"));

        return toResponse(user);
    }

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

        accountLockoutService.resetFailedAttempts(user);
        userRepository.save(user);
    }

    @Override
    public void logout() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UnauthorizedException("Current user not found"));

        refreshTokenRepository.revokeAllTokensByUser(user, LocalDateTime.now(clock));
    }

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

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        String rawToken = generateRefreshTokenValue();
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpiresAt(LocalDateTime.now(clock).plusDays(refreshTokenExpiryDays));
        refreshTokenRepository.save(refreshToken);
        return rawToken;
    }


    private String generateRefreshTokenValue() {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes());
    }

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

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isEmailVerified()
        );
    }
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

    @Override
    public void sendPhoneOtp(String email, String phone) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPhone(phone);
        userRepository.save(user);
        phoneOtpService.generateAndSendOtp(phone);
    }

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
    
}
