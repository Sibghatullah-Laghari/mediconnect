package com.mediconnect.service;

import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.dto.auth.UserResponse;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.DuplicateEmailException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse registerUser(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("User email already exists");
        }

        if (request.role() == Role.ADMIN) {
            throw new BadRequestException("Cannot self-assign admin role");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setEmailVerified(false);

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!canAccessUser(user)) {
            throw new UnauthorizedException("You do not have permission to view this user");
        }

        return toResponse(user);
    }

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        SecurityUtils.requireRole(Role.ADMIN);

        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    @Override
    public UserResponse updateUser(Long id, RegisterUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!canAccessUser(user)) {
            throw new UnauthorizedException("You do not have permission to update this user");
        }

        if (request.role() == Role.ADMIN && !SecurityUtils.hasRole(Role.ADMIN)) {
            throw new BadRequestException("Cannot self-assign admin role");
        }

        if (userRepository.existsByEmail(request.email()) && !user.getEmail().equalsIgnoreCase(request.email())) {
            throw new DuplicateEmailException("User email already exists");
        }
        
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(SecurityUtils.hasRole(Role.ADMIN) ? request.role() : user.getRole());

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!canAccessUser(user) && !SecurityUtils.hasRole(Role.ADMIN)) {
            throw new UnauthorizedException("You do not have permission to delete this user");
        }
        userRepository.delete(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isEmailVerified()
        );
    }

    private boolean canAccessUser(User user) {
        if (SecurityUtils.hasRole(Role.ADMIN)) {
            return true;
        }

        try {
            return SecurityUtils.getCurrentUserEmail().equalsIgnoreCase(user.getEmail());
        } catch (UnauthorizedException ex) {
            return false;
        }
    }
}
