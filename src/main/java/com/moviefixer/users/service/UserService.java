package com.moviefixer.users.service;

import com.moviefixer.users.dto.CreateUserRequest;
import com.moviefixer.users.dto.RegisterUserRequest;
import com.moviefixer.users.dto.UserResponse;

import java.util.UUID;

public interface UserService {

    /**
     * Public user registration endpoint for customers.
     * Enforces CUSTOMER role, PENDING status, and BCrypt password encryption.
     */
    UserResponse registerCustomer(RegisterUserRequest request);

    /**
     * Administrative user creation with custom role assignment.
     */
    UserResponse createUser(CreateUserRequest request);

    /**
     * Find a user profile by username.
     */
    UserResponse getUserByUsername(String username);

    /**
     * Find a user profile by ID.
     */
    UserResponse getUserById(UUID id);
}
