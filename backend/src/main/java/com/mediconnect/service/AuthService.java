package com.mediconnect.service;

import com.mediconnect.dto.auth.AuthResponse;
import com.mediconnect.dto.auth.LoginRequest;
import com.mediconnect.dto.auth.RefreshTokenRequest;
import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.dto.auth.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterUserRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    UserResponse getCurrentUser();
    void sendOTP(String email);
    AuthResponse verifyOTP(String email, String otp);
    void authenticate(String email, String password);
     void logout();
    void resendVerification(String email);
    void sendPhoneOtp(String email, String phone);
    void verifyPhoneOtp(String email, String phone, String otp);
}
