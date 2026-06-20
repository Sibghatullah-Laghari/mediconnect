package com.mediconnect.controller;

import com.mediconnect.config.SecurityConfig;
import com.mediconnect.dto.auth.UserResponse;
import com.mediconnect.model.Role;
import com.mediconnect.security.CustomUserDetailsService;
import com.mediconnect.security.JwtAuthenticationFilter;
import com.mediconnect.security.JwtService;
import com.mediconnect.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void userEndpointsRequireAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanReadUsers() throws Exception {
        when(userService.getUserById(1L)).thenReturn(new UserResponse(1L, "admin@example.com", Role.ADMIN));

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk());
    }
}
