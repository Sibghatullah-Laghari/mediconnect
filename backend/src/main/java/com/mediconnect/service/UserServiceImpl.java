package com.mediconnect.service;

import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.dto.auth.UserResponse;
import com.mediconnect.event.UserRegisteredEvent;
import com.mediconnect.exception.BadRequestException;
import com.mediconnect.exception.DuplicateEmailException;
import com.mediconnect.exception.ResourceNotFoundException;
import com.mediconnect.exception.UnauthorizedException;
import com.mediconnect.model.Role;
import com.mediconnect.model.User;
import com.mediconnect.repository.UserRepository;
import com.mediconnect.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for managing user accounts.
 * <p>
 * Provides user registration, retrieval, update, and deletion operations.
 * Enforces role‑based access control (ADMIN can manage all users; non‑admins
 * can only access/update their own account). Publishes a {@link UserRegisteredEvent}
 * upon successful registration to trigger email verification.
 * </p>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Registers a new user.
     * <p>
     * Validates that the email is not already taken and that the role is not ADMIN
     * (self‑assignment of admin is forbidden). Encrypts the password, saves the user,
     * and publishes a {@link UserRegisteredEvent} to trigger email verification.
     * </p>
     *
     * @param request the registration request containing user details
     * @return UserResponse containing the newly created user's information
     * @throws DuplicateEmailException if the email already exists
     * @throws BadRequestException if the role is ADMIN
     */
    @Override
    public UserResponse registerUser(RegisterUserRequest request) {
        log.info("Registering user: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration failed: Email already exists: {}", request.email());
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
        eventPublisher.publishEvent(new UserRegisteredEvent(saved));
        return toResponse(saved);
    }

    /**
     * Retrieves a user by ID.
     * <p>
     * Access is granted to ADMINs or to the user themselves.
     * </p>
     *
     * @param id the ID of the user to retrieve
     * @return UserResponse containing user details
     * @throws ResourceNotFoundException if the user does not exist
     * @throws UnauthorizedException if the current user lacks access
     */
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!canAccessUser(user)) {
            throw new UnauthorizedException("You do not have permission to view this user");
        }

        return toResponse(user);
    }

    /**
     * Retrieves all users with pagination. Accessible only to ADMINs.
     *
     * @param pageable pagination information
     * @return a page of UserResponse objects
     * @throws UnauthorizedException if the current user is not an ADMIN
     */
    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        SecurityUtils.requireRole(Role.ADMIN);

        return userRepository.findAll(pageable)
                .map(this::toResponse);
    }

    /**
     * Updates an existing user's details.
     * <p>
     * Only ADMINs or the user themselves can update the account.
     * If the user is not an ADMIN, they cannot change the role to ADMIN.
     * Email uniqueness is enforced when the email is changed.
     * </p>
     *
     * @param id the ID of the user to update
     * @param request the update request
     * @return the updated UserResponse
     * @throws ResourceNotFoundException if the user does not exist
     * @throws UnauthorizedException if the current user lacks permission
     * @throws DuplicateEmailException if the new email is already in use
     * @throws BadRequestException if a non‑ADMIN tries to assign the ADMIN role
     */
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

    /**
     * Deletes a user account.
     * <p>
     * Only ADMINs or the user themselves can delete the account.
     * </p>
     *
     * @param id the ID of the user to delete
     * @throws ResourceNotFoundException if the user does not exist
     * @throws UnauthorizedException if the current user lacks permission
     */
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (!canAccessUser(user) && !SecurityUtils.hasRole(Role.ADMIN)) {
            throw new UnauthorizedException("You do not have permission to delete this user");
        }
        userRepository.delete(user);
    }

    // ----- PRIVATE HELPER METHODS -----

    /**
     * Converts a User entity to a UserResponse DTO.
     *
     * @param user the user entity
     * @return the response DTO
     */
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.isEmailVerified()
        );
    }

    /**
     * Checks whether the current user has access to the given user account.
     * <p>
     * Access is granted if the current user is an ADMIN, or if the current user
     * is the same as the target user (matching email).
     * </p>
     *
     * @param user the target user
     * @return true if access is allowed, false otherwise
     */
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