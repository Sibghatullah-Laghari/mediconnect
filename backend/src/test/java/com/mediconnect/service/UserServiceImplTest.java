package com.mediconnect.service;

import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.DuplicateEmailException;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser_persistsPatientRole() {
        var request = new RegisterUserRequest("Jane", "jane@example.com", "password123", Role.PATIENT);
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = userService.registerUser(request);

        assertThat(response.email()).isEqualTo("jane@example.com");
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Jane");
        verify(emailService).sendVerificationEmail("jane@example.com", captor.getValue().getVerificationCode());
    }

    @Test
    void registerUser_rejectsDuplicateEmail() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);
        var request = new RegisterUserRequest("Jane", "jane@example.com", "password123", Role.PATIENT);

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void registerUser_rejectsAdminSelfRegistration() {
        var request = new RegisterUserRequest("Admin", "admin@example.com", "password123", Role.ADMIN);

        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(BadRequestException.class);
    }
}
