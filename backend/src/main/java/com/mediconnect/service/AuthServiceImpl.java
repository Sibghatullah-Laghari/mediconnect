package com.mediconnect.service;

import com.mediconnect.dto.auth.AuthResponse;
import com.mediconnect.dto.auth.LoginRequest;
import com.mediconnect.dto.auth.RefreshTokenRequest;
import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.dto.auth.UserResponse;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.EmailVerificationToken;
import com.mediconnect.model.RefreshToken;
import com.mediconnect.model.User;
import com.mediconnect.repository.EmailVerificationTokenRepository;
import com.mediconnect.repository.RefreshTokenRepository;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.security.CustomUserDetailsService;
import com.mediconnect.security.JwtService;
import com.mediconnect.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class AuthServiceImpl implements AuthService {

    private static final Duration OTP_EXPIRY = Duration.ofMinutes(10);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final JavaMailSender mailSender;
    private final Clock clock;
    private final AccountLockoutService accountLockoutService;

    @Value("${jwt.refresh-token.expiry.days}")
    private long refreshTokenExpiryDays;

    @Override
    public AuthResponse register(RegisterUserRequest request) {
        userService.registerUser(request);
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.email()));
        return buildAuthResponse(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticate(request.email(), request.password());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

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
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(currentEmail)
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

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("MediConnect verification code");
        message.setText("Your MediConnect OTP is " + otp + ". It expires in 10 minutes.");
        mailSender.send(message);
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
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        accountLockoutService.checkLockout(user);

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            accountLockoutService.recordFailedAttempt(user);
            userRepository.save(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        accountLockoutService.resetFailedAttempts(user);
        userRepository.save(user);
    }

    @Override
    public void logout() {
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UnauthorizedException("Current user not found"));

        refreshTokenRepository.revokeAllTokensByUser(user, LocalDateTime.now(clock));
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        pruneExpiredTokens(user);

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

    private void pruneExpiredTokens(User user) {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now(clock));
    }

    private String generateRefreshTokenValue() {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes());
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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
}