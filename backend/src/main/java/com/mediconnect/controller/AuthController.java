package com.mediconnect.controller;

import com.mediconnect.dto.auth.AuthResponse;
import com.mediconnect.dto.auth.EmailRequest;
import com.mediconnect.dto.auth.LoginRequest;
import com.mediconnect.dto.auth.OTPRequest;
import com.mediconnect.dto.auth.RefreshTokenRequest;
import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.dto.auth.UserResponse;
import com.mediconnect.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller responsible for authentication and authorization operations.
 *
 * <p>Provides endpoints for:
 * <ul>
 *     <li>User registration</li>
 *     <li>User login</li>
 *     <li>JWT token refresh</li>
 *     <li>Current user retrieval</li>
 *     <li>OTP generation and verification</li>
 *     <li>User logout</li>
 * </ul>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Registers a new user account.
     *
     * @param request user registration details
     * @return authentication response containing access and refresh tokens
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    /**
     * Authenticates a user using email and password.
     *
     * @param request login credentials
     * @return authentication response containing JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Generates a new access token using a valid refresh token.
     *
     * @param request refresh token request
     * @return new authentication response with refreshed tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    /**
     * Retrieves details of the currently authenticated user.
     *
     * @return current user's profile information
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    /**
     * Sends a One-Time Password (OTP) to the provided email address.
     *
     * @param request email address request
     * @return HTTP 204 No Content on successful OTP delivery
     */
    @PostMapping("/send-otp")
    public ResponseEntity<Void> sendOTP(
            @Valid @RequestBody EmailRequest request) {
        authService.sendOTP(request.email());
        return ResponseEntity.noContent().build();
    }

    /**
     * Verifies the OTP sent to the user's email.
     *
     * @param request email and OTP verification request
     * @return authentication response if verification succeeds
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOTP(
            @Valid @RequestBody OTPRequest request) {
        return ResponseEntity.ok(
                authService.verifyOTP(request.email(), request.otp())
        );
    }

    /**
     * Logs out the currently authenticated user and invalidates
     * the active session/refresh token.
     *
     * @return HTTP 204 No Content on successful logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
    }
}