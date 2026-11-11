package com.moviefixer.auth.controller;

import com.moviefixer.auth.dto.*;
import com.moviefixer.auth.dto.*;
import com.moviefixer.auth.service.AuthService;
import com.moviefixer.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user login, rotating token refresh, logout, and email verification")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user with username/email and password", description = "Returns short-lived JWT Access Token and rotating Refresh Token upon successful credential check.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rotate refresh token and issue new access token", description = "Validates the active refresh token, revokes it to prevent reuse, and returns a new access & refresh token pair.")
    public ResponseEntity<ApiResponse<TokenRefreshResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenRefreshResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user and revoke refresh token", description = "Revokes the supplied refresh token so it can no longer be used to generate access tokens.")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null && request.getRefreshToken() != null) {
            authService.logout(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResponse.successMessage("Logged out successfully"));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify account email using token (POST)", description = "Activates a PENDING account by validating the verification token.")
    public ResponseEntity<ApiResponse<Void>> verifyEmailPost(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getToken());
        return ResponseEntity.ok(ApiResponse.successMessage("Account verified and activated successfully. You may now log in."));
    }

    @GetMapping("/verify")
    @Operation(summary = "Verify account email using token (GET)", description = "Allows activating account via direct email link click.")
    public ResponseEntity<ApiResponse<Void>> verifyEmailGet(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.successMessage("Account verified and activated successfully. You may now log in."));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend account verification token", description = "Generates and sends a new verification token for accounts in PENDING status.")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.getEmail());
            return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.successMessage("Verification token resent successfully. Please check your email."));
    }
}
