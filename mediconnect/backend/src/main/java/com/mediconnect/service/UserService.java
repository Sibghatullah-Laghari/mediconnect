package com.mediconnect.service;

import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.dto.auth.UserResponse;
import com.mediconnect.model.User;
import java.util.List;

public interface UserService {
    UserResponse registerUser(RegisterUserRequest request);
    User registerPublicPatient(RegisterUserRequest request);
    UserResponse getUserById(Long id);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long id, RegisterUserRequest request);
    void deleteUser(Long id);
}
