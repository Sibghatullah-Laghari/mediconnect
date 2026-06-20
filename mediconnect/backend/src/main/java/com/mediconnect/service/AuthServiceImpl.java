package com.mediconnect.service;

import com.mediconnect.dto.auth.AuthResponse;
import com.mediconnect.dto.auth.LoginRequest;
import com.mediconnect.dto.auth.RefreshTokenRequest;
import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.dto.auth.UserResponse;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.RefreshToken;
import com.mediconnect.model.User;
import com.mediconnect.repository.RefreshTokenRepository;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.security.CustomUserDetailsService;
import com.mediconnect.security.JwtService;
import com.mediconnect.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final Clock clock;

    @Override
    public AuthResponse register(RegisterUserRequest request) {
        User user = userService.registerPublicPatient(request);
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
        LocalDateTime now = LocalDateTime.now(clock);
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new UnauthorizedException("Refresh token is invalid or expired"));

        if (storedToken.getRevokedAt() != null || storedToken.getExpiresAt().isBefore(now)) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        storedToken.setRevokedAt(now);
        User user = storedToken.getUser();
        return buildAuthResponse(user);
    }

    @Override
    public UserResponse getCurrentUser() {
        String currentEmail = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new UnauthorizedException("Current user not found"));

        return new UserResponse(user.getId(), user.getEmail(), user.getRole());
    }

    @Override
    public void authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        pruneExpiredTokens(user);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = createRefreshToken(user);
        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(generateRefreshTokenValue());
        refreshToken.setExpiresAt(LocalDateTime.now(clock).plus(Duration.ofMillis(jwtService.getRefreshTokenExpirationMs())));
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    private void pruneExpiredTokens(User user) {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now(clock));
        refreshTokenRepository.deleteByUserAndRevokedAtIsNotNull(user);
    }

    private String generateRefreshTokenValue() {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(UUID.randomUUID().toString().getBytes());
    }
}
