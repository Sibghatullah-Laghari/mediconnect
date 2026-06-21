package com.mediconnect.controller;

import com.mediconnect.dto.auth.AuthResponse;
import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.dto.auth.UserResponse;
import com.mediconnect.model.Role;
import com.mediconnect.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void registerReturnsCreatedResponse() {
        AuthService authService = mock(AuthService.class);
        AuthController controller = new AuthController(authService);
        AuthResponse expected = new AuthResponse(
                "token",
                "refresh",
                new UserResponse(1L, "user@example.com", "User", Role.PATIENT, false)
        );

        when(authService.register(any())).thenReturn(expected);

        var response = controller.register(new RegisterUserRequest(
                "User",
                "user@example.com",
                "Pass1234",
                Role.PATIENT
        ));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertSame(expected, response.getBody());
        verify(authService).register(any());
    }
}
