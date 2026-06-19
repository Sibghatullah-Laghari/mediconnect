package com.mediconnect.service;

import com.mediconnect.dto.auth.AuthResponse;
import com.mediconnect.dto.auth.LoginRequest;
import com.mediconnect.dto.auth.RefreshTokenRequest;
import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.dto.auth.UserResponse;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.security.CustomUserDetailsService;
import com.mediconnect.security.JwtService;
import com.mediconnect.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    public AuthResponse register(RegisterUserRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new UnauthorizedException("Public registration cannot create admin users");
        }

        UserResponse createdUser = userService.registerUser(request);
        User user = userRepository.findById(createdUser.id())
                .orElseThrow(() -> new UnauthorizedException("Unable to create authenticated session"));

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
        String refreshToken = request.refreshToken();
        String email = jwtService.extractUsername(refreshToken);
        if (email == null) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        if (!jwtService.isRefreshToken(refreshToken) || !jwtService.isTokenValid(refreshToken, userDetails)) {
            throw new UnauthorizedException("Refresh token is invalid or expired");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

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
        return new AuthResponse(
                jwtService.generateAccessToken(userDetails),
                jwtService.generateRefreshToken(userDetails),
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );
    }
}
