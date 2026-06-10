package com.mediconnect.service;

import com.mediconnect.dto.auth.AuthResponse;
import com.mediconnect.dto.auth.ForgotPasswordRequest;
import com.mediconnect.dto.auth.LoginRequest;
import com.mediconnect.dto.auth.RefreshTokenRequest;
import com.mediconnect.dto.auth.ResetPasswordRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void verifyEmail(String email, String code);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
