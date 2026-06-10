package com.mediconnect.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediconnect.dto.auth.LoginRequest;
import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.model.Role;
import com.mediconnect.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void registerAndLoginFlow() throws Exception {
        var register = new RegisterUserRequest("Jane Doe", "jane@example.com", "password123", Role.PATIENT);
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/verify-email")
                        .param("email", "jane@example.com")
                        .param("code", "000000"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("jane@example.com", "password123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerRejectsAdminRole() throws Exception {
        var register = new RegisterUserRequest("Admin", "admin@example.com", "password123", Role.ADMIN);
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Admin accounts cannot be self-registered"));
    }
}
