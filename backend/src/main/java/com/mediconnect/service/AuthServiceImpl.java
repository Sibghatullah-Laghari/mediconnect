package com.mediconnect.service;

import com.mediconnect.dto.auth.AuthResponse;
import com.mediconnect.dto.auth.LoginRequest;
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

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (user.isAccountLocked()) {
            throw new UnauthorizedException("Account locked. Try again after 30 minutes.");
        }

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Email not verified. Check your inbox for verification link.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            user.setLoginAttempts(user.getLoginAttempts() + 1);
            user.setLastFailedLoginTime(LocalDateTime.now());

            if (user.getLoginAttempts() >= 5) {
                user.setAccountLocked(true);
                userRepository.save(user);
                throw new UnauthorizedException("Account locked after 5 failed attempts. Try again after 30 minutes.");
            }

            userRepository.save(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        user.setLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLastFailedLoginTime(null);

        String accessToken = jwtTokenProvider.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());

        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return new AuthResponse(accessToken, refreshToken, user.getEmail(), user.getRole().name());
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
}
