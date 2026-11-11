package com.moviefixer.auth.service;

import com.moviefixer.auth.dto.AuthResponse;
import com.moviefixer.auth.dto.LoginRequest;
import com.moviefixer.auth.dto.RefreshTokenRequest;
import com.moviefixer.auth.dto.TokenRefreshResponse;
import com.moviefixer.auth.entity.RefreshToken;
import com.moviefixer.auth.entity.VerificationToken;
import com.moviefixer.auth.exception.AccountNotVerifiedException;
import com.moviefixer.auth.exception.InvalidTokenException;
import com.moviefixer.auth.repository.RefreshTokenRepository;
import com.moviefixer.auth.repository.VerificationTokenRepository;
import com.moviefixer.auth.security.JwtTokenProvider;
import com.moviefixer.users.entity.Role;
import com.moviefixer.users.entity.Users;
import com.moviefixer.users.enums.AccountStatus;
import com.moviefixer.users.enums.USER_ROLE;
import com.moviefixer.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthServiceImpl authService;

    private Users activeUser;
    private Users pendingUser;

    @BeforeEach
    void setUp() {
        Role customerRole = new Role(USER_ROLE.CUSTOMER, "Customer");
        customerRole.setId(UUID.randomUUID());

        activeUser = new Users();
        activeUser.setId(UUID.randomUUID());
        activeUser.setUsername("johndoe");
        activeUser.setEmail("john.doe@example.com");
        activeUser.setMobile("+919876543210");
        activeUser.setPasswordHash("hashedPassword123");
        activeUser.setAccountStatus(AccountStatus.ACTIVE);
        activeUser.addRole(customerRole);

        pendingUser = new Users();
        pendingUser.setId(UUID.randomUUID());
        pendingUser.setUsername("janedoe");
        pendingUser.setEmail("jane.doe@example.com");
        pendingUser.setMobile("+919876543211");
        pendingUser.setPasswordHash("hashedPassword123");
        pendingUser.setAccountStatus(AccountStatus.PENDING);
        pendingUser.addRole(customerRole);
    }

    @Test
    @DisplayName("Login succeeds with valid credentials for ACTIVE user")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("johndoe", "rawPassword123");

        when(userRepository.findByUsernameOrEmail("johndoe", "johndoe")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("rawPassword123", "hashedPassword123")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(activeUser)).thenReturn("mock.jwt.token");
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("johndoe", response.getUser().getUsername());
        verify(userRepository).save(activeUser);
        assertNotNull(activeUser.getLastLoginAt());
    }

    @Test
    @DisplayName("Login fails with AccountNotVerifiedException when user status is PENDING")
    void testLogin_PendingAccount_ThrowsException() {
        LoginRequest request = new LoginRequest("janedoe", "rawPassword123");

        when(userRepository.findByUsernameOrEmail("janedoe", "janedoe")).thenReturn(Optional.of(pendingUser));
        when(passwordEncoder.matches("rawPassword123", "hashedPassword123")).thenReturn(true);

        assertThrows(AccountNotVerifiedException.class, () -> authService.login(request));
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login fails with BadCredentialsException when password does not match")
    void testLogin_BadPassword_ThrowsException() {
        LoginRequest request = new LoginRequest("johndoe", "wrongPassword");

        when(userRepository.findByUsernameOrEmail("johndoe", "johndoe")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword123")).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Refresh token succeeds and rotates old token")
    void testRefreshToken_Success() {
        String oldTokenString = "old-refresh-token";
        RefreshToken oldToken = new RefreshToken(activeUser, oldTokenString, Instant.now().plusSeconds(3600));

        when(refreshTokenRepository.findByToken(oldTokenString)).thenReturn(Optional.of(oldToken));
        when(jwtTokenProvider.getRefreshTokenExpirationMs()).thenReturn(604800000L);
        when(jwtTokenProvider.getAccessTokenExpirationMs()).thenReturn(900000L);
        when(jwtTokenProvider.generateAccessToken(activeUser)).thenReturn("new.jwt.token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        TokenRefreshResponse response = authService.refreshToken(new RefreshTokenRequest(oldTokenString));

        assertNotNull(response);
        assertEquals("new.jwt.token", response.getAccessToken());
        assertNotEquals(oldTokenString, response.getRefreshToken());
        assertTrue(oldToken.isRevoked());
    }

    @Test
    @DisplayName("Refresh token reuse detected: invalidates all user active sessions")
    void testRefreshToken_ReuseDetected_RevokesAllSessions() {
        String revokedTokenString = "stolen-revoked-token";
        RefreshToken revokedToken = new RefreshToken(activeUser, revokedTokenString, Instant.now().plusSeconds(3600));
        revokedToken.setRevoked(true);

        when(refreshTokenRepository.findByToken(revokedTokenString)).thenReturn(Optional.of(revokedToken));

        assertThrows(InvalidTokenException.class, () ->
                authService.refreshToken(new RefreshTokenRequest(revokedTokenString)));

        verify(refreshTokenRepository).revokeAllActiveByUser(activeUser);
    }

    @Test
    @DisplayName("Verify email activates account from PENDING to ACTIVE")
    void testVerifyEmail_Success() {
        String tokenString = "valid-verification-token";
        VerificationToken token = new VerificationToken(pendingUser, tokenString, Instant.now().plusSeconds(86400));

        when(verificationTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));

        authService.verifyEmail(tokenString);

        assertTrue(token.isUsed());
        assertEquals(AccountStatus.ACTIVE, pendingUser.getAccountStatus());
        assertNotNull(pendingUser.getEmailVerifiedAt());
        verify(userRepository).save(pendingUser);
    }
}
