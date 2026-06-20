package com.mediconnect.service;

import com.mediconnect.dto.auth.AuthResponse;
import com.mediconnect.dto.auth.LoginRequest;
import com.mediconnect.dto.auth.RefreshTokenRequest;
import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.RefreshToken;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.RefreshTokenRepository;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.security.CustomUserDetailsService;
import com.mediconnect.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    private Clock clock;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-06-20T10:15:30Z"), ZoneOffset.UTC);
        authService = new AuthServiceImpl(
                userRepository,
                userService,
                refreshTokenRepository,
                passwordEncoder,
                userDetailsService,
                jwtService,
                clock
        );
    }

    @Test
    void registerIgnoresRequestedRoleAndCreatesPatientSession() {
        RegisterUserRequest request = new RegisterUserRequest("Alice Jones", "alice@example.com", "Pass1234", Role.ADMIN);
        User user = buildUser(7L, "alice@example.com", Role.PATIENT, "encoded-password");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("encoded-password")
                .roles("PATIENT")
                .build();

        when(userService.registerPublicPatient(request)).thenReturn(user);
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(86_400_000L);

        AuthResponse response = authService.register(request);

        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.role()).isEqualTo("PATIENT");
        assertThat(response.token()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        verify(userService).registerPublicPatient(request);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).findByToken(anyString());
    }

    @Test
    void refreshRotatesValidStoredRefreshToken() {
        User user = buildUser(9L, "doctor@example.com", Role.DOCTOR, "encoded-password");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("encoded-password")
                .roles("DOCTOR")
                .build();
        RefreshToken storedToken = new RefreshToken();
        storedToken.setUser(user);
        storedToken.setToken("stored-refresh-token");
        storedToken.setExpiresAt(LocalDateTime.now(clock).plusDays(2));

        when(refreshTokenRepository.findByToken("stored-refresh-token")).thenReturn(Optional.of(storedToken));
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("next-access-token");
        when(jwtService.getRefreshTokenExpirationMs()).thenReturn(86_400_000L);

        AuthResponse response = authService.refresh(new RefreshTokenRequest("stored-refresh-token"));

        assertThat(response.token()).isEqualTo("next-access-token");
        assertThat(response.refreshToken()).isNotEqualTo("stored-refresh-token");
        assertThat(storedToken.getRevokedAt()).isEqualTo(LocalDateTime.now(clock));

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
        assertThat(refreshTokenCaptor.getValue().getUser()).isEqualTo(user);
    }

    @Test
    void refreshRejectsRevokedToken() {
        RefreshToken storedToken = new RefreshToken();
        storedToken.setToken("revoked-token");
        storedToken.setExpiresAt(LocalDateTime.now(clock).plusDays(1));
        storedToken.setRevokedAt(LocalDateTime.now(clock).minusMinutes(5));

        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refresh(new RefreshTokenRequest("revoked-token")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Refresh token is invalid or expired");
    }

    @Test
    void loginRejectsInvalidPassword() {
        User user = buildUser(4L, "patient@example.com", Role.PATIENT, "encoded-password");
        when(userRepository.findByEmail("patient@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-pass", "encoded-password")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("patient@example.com", "wrong-pass")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Invalid email or password");
    }

    private User buildUser(Long id, String email, Role role, String passwordHash) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setRole(role);

        try {
            var idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to assign test user id", exception);
        }

        return user;
    }
}
