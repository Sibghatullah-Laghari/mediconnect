package com.mediconnect.service;

import com.mediconnect.dto.auth.LoginRequest;
import com.mediconnect.dto.auth.RefreshTokenRequest;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User verifiedUser;

    @BeforeEach
    void setUp() {
        verifiedUser = new User();
        verifiedUser.setId(1L);
        verifiedUser.setEmail("patient@example.com");
        verifiedUser.setPasswordHash("hash");
        verifiedUser.setRole(Role.PATIENT);
        verifiedUser.setEmailVerified(true);
        verifiedUser.setAccountLocked(false);
    }

    @Test
    void login_returnsTokensForValidCredentials() {
        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(verifiedUser));
        when(passwordEncoder.matches("password", "hash")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(anyString(), anyString())).thenReturn("access");
        when(jwtTokenProvider.generateRefreshToken(anyString())).thenReturn("refresh");

        var response = authService.login(new LoginRequest("patient@example.com", "password"));

        assertThat(response.accessToken()).isEqualTo("access");
        assertThat(response.refreshToken()).isEqualTo("refresh");
        verify(userRepository).save(verifiedUser);
    }

    @Test
    void login_locksAccountAfterFiveFailedAttempts() {
        verifiedUser.setLoginAttempts(4);
        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(verifiedUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("patient@example.com", "wrong")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("locked");
    }

    @Test
    void refreshToken_rejectsRevokedToken() {
        verifiedUser.setRefreshToken("stored-refresh");
        when(jwtTokenProvider.isTokenValid("other-token")).thenReturn(true);
        when(jwtTokenProvider.extractEmail("other-token")).thenReturn("patient@example.com");
        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(verifiedUser));

        assertThatThrownBy(() -> authService.refreshToken(new RefreshTokenRequest("other-token")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("revoked");
    }
}
