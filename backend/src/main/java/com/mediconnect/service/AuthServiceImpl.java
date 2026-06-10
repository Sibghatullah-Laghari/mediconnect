package com.mediconnect.service;

import com.mediconnect.dto.auth.AuthResponse;
import com.mediconnect.dto.auth.ForgotPasswordRequest;
import com.mediconnect.dto.auth.LoginRequest;
import com.mediconnect.dto.auth.RefreshTokenRequest;
import com.mediconnect.dto.auth.ResetPasswordRequest;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.User;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        unlockIfExpired(user);

        if (user.isAccountLocked()) {
            throw new UnauthorizedException("Account locked. Try again after 30 minutes.");
        }

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Email not verified. Check your inbox for verification link.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        resetLoginAttempts(user);
        return issueTokens(user);
    }

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        if (!jwtTokenProvider.isTokenValid(request.refreshToken())) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = jwtTokenProvider.extractEmail(request.refreshToken());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!request.refreshToken().equals(user.getRefreshToken())) {
            throw new UnauthorizedException("Refresh token revoked");
        }

        return issueTokens(user);
    }

    @Override
    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));

        if (!code.equals(user.getVerificationCode())) {
            throw new BadRequestException("Invalid verification code");
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        userRepository.save(user);
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            user.setPasswordResetToken(token);
            user.setPasswordResetExpiry(LocalDateTime.now().plusHours(1));
            userRepository.save(user);
            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPasswordResetToken(request.token())
                .orElseThrow(() -> new BadRequestException("Invalid or expired reset token"));

        if (user.getPasswordResetExpiry() == null
                || user.getPasswordResetExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiry(null);
        user.setRefreshToken(null);
        userRepository.save(user);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole().name());
    }

    private void unlockIfExpired(User user) {
        if (user.isAccountLocked()
                && user.getLastFailedLoginTime() != null
                && user.getLastFailedLoginTime().plusMinutes(LOCK_DURATION_MINUTES).isBefore(LocalDateTime.now())) {
            user.setAccountLocked(false);
            user.setLoginAttempts(0);
            user.setLastFailedLoginTime(null);
            userRepository.save(user);
        }
    }

    private void handleFailedLogin(User user) {
        user.setLoginAttempts(user.getLoginAttempts() + 1);
        user.setLastFailedLoginTime(LocalDateTime.now());

        if (user.getLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
            user.setAccountLocked(true);
            userRepository.save(user);
            throw new UnauthorizedException("Account locked after 5 failed attempts. Try again after 30 minutes.");
        }

        userRepository.save(user);
    }

    private void resetLoginAttempts(User user) {
        user.setLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLastFailedLoginTime(null);
    }
}
