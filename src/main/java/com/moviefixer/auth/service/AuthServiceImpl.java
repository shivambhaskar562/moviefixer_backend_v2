package com.moviefixer.auth.service;

import com.moviefixer.auth.dto.*;
import com.moviefixer.auth.entity.RefreshToken;
import com.moviefixer.auth.entity.VerificationToken;
import com.moviefixer.auth.exception.AccountNotVerifiedException;
import com.moviefixer.auth.exception.InvalidTokenException;
import com.moviefixer.auth.repository.RefreshTokenRepository;
import com.moviefixer.auth.repository.VerificationTokenRepository;
import com.moviefixer.auth.security.JwtTokenProvider;
import com.moviefixer.auth.security.PasswordHashProvider;
import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.users.entity.Users;
import com.moviefixer.users.enums.AccountStatus;
import com.moviefixer.users.event.UserRegisteredEvent;
import com.moviefixer.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordHashProvider passwordHashProvider;

    @Value("${app.auth.verification-base-url:${frontend.url}/verify-email}")
    private String verificationBaseUrl;

    public AuthServiceImpl(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            VerificationTokenRepository verificationTokenRepository,
            JwtTokenProvider jwtTokenProvider,
            PasswordHashProvider passwordHashProvider,
            ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordHashProvider = passwordHashProvider;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login attempt for identifier: {}", request.getUsernameOrEmail());

        Users user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password"));

        if (!passwordHashProvider.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Failed login attempt for user: {}", user.getUsername());
            throw new BadCredentialsException("Invalid username/email or password");
        }

        if (user.getAccountStatus() == AccountStatus.PENDING) {
            log.warn("Login rejected: user {} is still PENDING verification", user.getUsername());
            throw new AccountNotVerifiedException("Account is pending verification. Please verify your email before logging in.");
        }

        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            log.warn("Login rejected: user {} is SUSPENDED", user.getUsername());
            throw new DisabledException("Account has been suspended. Please contact support.");
        }

        if (user.getAccountStatus() == AccountStatus.DISABLED) {
            log.warn("Login rejected: user {} is DISABLED", user.getUsername());
            throw new DisabledException("Account has been deactivated.");
        }

        // Record last login activity
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // Generate Access Token
        String accessToken = jwtTokenProvider.generateAccessToken(user);

        // Generate and persist rotating Refresh Token
        String refreshTokenString = UUID.randomUUID().toString();
        Instant refreshExpiry = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());
        RefreshToken refreshToken = new RefreshToken(user, refreshTokenString, refreshExpiry);
        refreshTokenRepository.save(refreshToken);

        // Map safe User Summary
        Set<String> roles = user.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toSet());

        UserSummaryResponse userSummary = new UserSummaryResponse(
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getMobile(),
                user.getAccountStatus(),
                roles
        );

        log.info("User {} successfully logged in with role(s): {}", user.getUsername(), roles);

        return new AuthResponse(
                accessToken,
                refreshTokenString,
                jwtTokenProvider.getAccessTokenExpirationMs(),
                userSummary
        );
    }

    @Override
    @Transactional
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        log.info("Processing refresh token request");

        RefreshToken oldToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (oldToken.isRevoked()) {
            log.warn("SECURITY ALERT: Revoked refresh token reuse detected for user {}. Invalidating all active tokens.",
                    oldToken.getUser().getUsername());
            refreshTokenRepository.revokeAllActiveByUser(oldToken.getUser());
            throw new InvalidTokenException("Refresh token was previously revoked. All active sessions invalidated for security.");
        }

        if (oldToken.isExpired()) {
            log.warn("Expired refresh token presented for user: {}", oldToken.getUser().getUsername());
            throw new InvalidTokenException("Refresh token has expired. Please log in again.");
        }

        Users user = oldToken.getUser();
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new InvalidTokenException("User account is no longer active.");
        }

        // Rotate token
        String newRefreshTokenString = UUID.randomUUID().toString();
        Instant newExpiry = Instant.now().plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());
        RefreshToken newRefreshToken = new RefreshToken(user, newRefreshTokenString, newExpiry);
        refreshTokenRepository.save(newRefreshToken);

        oldToken.revoke(newRefreshTokenString);
        refreshTokenRepository.save(oldToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        log.info("Successfully rotated refresh token for user: {}", user.getUsername());

        return new TokenRefreshResponse(
                newAccessToken,
                newRefreshTokenString,
                jwtTokenProvider.getAccessTokenExpirationMs()
        );
    }

    @Override
    @Transactional
    public void logout(String refreshTokenString) {
        log.info("Processing logout request");

        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(refreshTokenString);
        if (tokenOpt.isPresent()) {
            RefreshToken token = tokenOpt.get();
            token.revoke("LOGOUT");
            refreshTokenRepository.save(token);
            log.info("Revoked refresh token for user: {}", token.getUser().getUsername());
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String tokenString) {
        log.info("Processing email verification with token");

        VerificationToken verificationToken = verificationTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token"));

        if (verificationToken.isUsed()) {
            log.warn("Verification token has already been used");
            throw new InvalidTokenException("Verification token has already been used.");
        }

        if (verificationToken.isExpired()) {
            log.warn("Verification token has expired");
            throw new InvalidTokenException("Verification token has expired. Please request a new one.");
        }

        verificationToken.markAsUsed();
        verificationTokenRepository.save(verificationToken);

        Users user = verificationToken.getUser();
        user.setEmailVerifiedAt(LocalDateTime.now());
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        log.info("Successfully verified email and activated account for user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void resendVerification(String email) {
        log.info("Resending verification email request for: {}", email);

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.getAccountStatus() == AccountStatus.ACTIVE) {
            log.info("User {} is already verified and active", email);
            return;
        }

        eventPublisher.publishEvent(new UserRegisteredEvent(user));
    }

    @Override
    @Transactional
    public VerificationToken createVerificationToken(Users user) {
        String tokenString = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plusMillis(jwtTokenProvider.getVerificationTokenExpirationMs());
        VerificationToken token = new VerificationToken(user, tokenString, expiry);
        VerificationToken saved = verificationTokenRepository.save(token);

        String baseUrl = (verificationBaseUrl != null && !verificationBaseUrl.isBlank())
                ? verificationBaseUrl
                : "";

        log.info("Generated account verification token for user {} [{}]: {} (Verification Link: {}?token={})",
                user.getUsername(), user.getEmail(), tokenString, baseUrl, tokenString);

        return saved;
    }
}
