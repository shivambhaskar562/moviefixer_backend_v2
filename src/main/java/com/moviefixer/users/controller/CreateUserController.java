package com.moviefixer.users.controller;

import com.moviefixer.users.dto.CreateUserRequest;
import com.moviefixer.users.dto.RegisterUserRequest;
import com.moviefixer.users.dto.UserResponse;
import com.moviefixer.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User Management", description = "Endpoints for user registration, creation, and profile retrieval")
public class CreateUserController {

    private final UserService userService;

    public CreateUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Public registration endpoint: Always assigns CUSTOMER role and PENDING status.
     * Prevents duplicate usernames, emails, and mobile numbers.
     */
    @Operation(
            summary = "Register a new customer",
            description = "Public registration endpoint that creates a user with CUSTOMER role and PENDING status. Passwords are encrypted using BCrypt."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Validation error in request payload"),
            @ApiResponse(responseCode = "409", description = "Username, email, or mobile number already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerCustomer(
            @Valid @RequestBody RegisterUserRequest request) {

        UserResponse response = userService.registerCustomer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Admin user creation: Allows administrators to create accounts with custom roles.
     */
    @Operation(
            summary = "Create user with roles (Admin only)",
            description = "Enables administrators to create user accounts with specific roles assigned."
    )
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse response = userService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Authenticated user profile lookup.
     */
    @Operation(summary = "Get current user profile", description = "Returns the profile of the authenticated user.")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            Principal principal) {

        UserResponse response = userService.getUserByUsername(principal.getName());

        return ResponseEntity.ok(response);
    }
}
