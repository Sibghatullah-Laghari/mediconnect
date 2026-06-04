package com.mediconnect.service;

import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.dto.auth.UserResponse;

public interface UserService {
    UserResponse registerUser(RegisterUserRequest request);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, RegisterUserRequest request);
    void deleteUser(Long id);
}
