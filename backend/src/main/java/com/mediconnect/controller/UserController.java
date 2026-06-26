package com.mediconnect.controller;

import com.mediconnect.dto.auth.RegisterUserRequest;
import com.mediconnect.dto.auth.UserResponse;
import com.mediconnect.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing users.
 * Provides endpoints for registering, retrieving, updating, and deleting users.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {


    /**
     * Service for handling user business logic.
     */
    private final UserService userService;

    /**
     * Registers a new user.
     *
     * @param request the user registration details
     * @return the registered user response
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterUserRequest request) {
        log.info("Registering new user with email: {}", request.email());
        UserResponse response = userService.registerUser(request);
        log.info("User registered successfully with ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves a user by ID.
     *
     * @param id the user ID
     * @return the user response
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        log.info("Fetching user with ID: {}", id);
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all users.
     *
     * @return a list of all user responses
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching all users, page: {}, size: {}", page, size);
        return ResponseEntity.ok(userService.getAllUsers(PageRequest.of(page, size)));
    }

    /**
     * Updates an existing user.
     *
     * @param id      the user ID
     * @param request the user update details
     * @return the updated user response
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody RegisterUserRequest request) {
        log.info("Updating user with ID: {}", id);
        UserResponse response = userService.updateUser(id, request);
        log.info("User with ID: {} updated successfully", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a user by ID.
     *
     * @param id the user ID
     * @return a response entity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with ID: {}", id);
        userService.deleteUser(id);
        log.info("User with ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}
