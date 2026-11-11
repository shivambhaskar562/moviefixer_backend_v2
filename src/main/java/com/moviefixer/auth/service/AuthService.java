package com.moviefixer.auth.service;

import com.moviefixer.auth.dto.AuthResponse;
import com.moviefixer.auth.dto.LoginRequest;
import com.moviefixer.auth.dto.RefreshTokenRequest;
import com.moviefixer.auth.dto.TokenRefreshResponse;
import com.moviefixer.auth.entity.VerificationToken;
import com.moviefixer.users.entity.Users;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    TokenRefreshResponse refreshToken(RefreshTokenRequest request);

    void logout(String refreshToken);

    void verifyEmail(String token);

    void resendVerification(String email);

    VerificationToken createVerificationToken(Users user);
}
