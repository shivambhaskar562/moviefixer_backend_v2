package com.moviefixer.auth.security;

import com.moviefixer.users.entity.Role;
import com.moviefixer.users.entity.Users;
import com.moviefixer.users.enums.AccountStatus;
import com.moviefixer.users.enums.USER_ROLE;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "4c7e6b8f1d2a3c5e7b9a0f2d4e6b8a1c3e5d7f9a0b2c4e6f8a1d3c5e7b9a0f2d";
    private Users user;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, 900000L, 604800000L, 86400000L);

        user = new Users();
        user.setId(UUID.randomUUID());
        user.setName("John Doe");
        user.setUsername("johndoe");
        user.setEmail("john.doe@example.com");
        user.setMobile("+919876543210");
        user.setAccountStatus(AccountStatus.ACTIVE);

        Role role = new Role(USER_ROLE.CUSTOMER, "Standard customer");
        role.setId(UUID.randomUUID());
        user.addRole(role);
    }

    @Test
    @DisplayName("Generate access token and validate successfully")
    void testGenerateAndValidateToken() {
        String token = jwtTokenProvider.generateAccessToken(user);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(user.getId(), jwtTokenProvider.getUserIdFromToken(token));
        assertEquals("johndoe", jwtTokenProvider.getUsernameFromToken(token));
        assertTrue(jwtTokenProvider.getRolesFromToken(token).contains("ROLE_CUSTOMER"));
    }

    @Test
    @DisplayName("Validate returns false for invalid token")
    void testValidateInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.token.structure"));
    }

    @Test
    @DisplayName("Expired token validation returns false")
    void testExpiredToken() {
        // Provider with negative expiration to simulate immediate expiration
        JwtTokenProvider expiredProvider = new JwtTokenProvider(secret, -1000L, 604800000L, 86400000L);
        String token = expiredProvider.generateAccessToken(user);

        assertFalse(jwtTokenProvider.validateToken(token));
    }
}
