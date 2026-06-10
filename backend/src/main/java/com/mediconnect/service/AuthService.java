package com.mediconnect.service;

import com.mediconnect.dto.auth.AuthResponse;
import com.mediconnect.dto.auth.LoginRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    void verifyEmail(String email, String code);
}
