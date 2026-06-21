package com.mediconnect.service;

import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.model.Role;
import com.mediconnect.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void registerUserRejectsAdminSelfAssignment() {
        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);

        UserServiceImpl service = new UserServiceImpl(userRepository, passwordEncoder);

        assertThrows(BadRequestException.class, () ->
                service.registerUser(new RegisterUserRequest(
                        "Admin User",
                        "admin@example.com",
                        "Pass1234",
                        Role.ADMIN
                ))
        );
    }
}
