package com.moviefixer.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviefixer.auth.dto.*;
import com.moviefixer.auth.service.AuthService;
import com.moviefixer.common.exception.GlobalExceptionHandler;
import com.moviefixer.users.enums.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/login returns 200 and auth payload on valid credentials")
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest("johndoe", "SecureP@ss123");
        UserSummaryResponse userSummary = new UserSummaryResponse(
                UUID.randomUUID(), "John Doe", "johndoe", "john@example.com", "+919876543210",
                AccountStatus.ACTIVE, Set.of("CUSTOMER")
        );
        AuthResponse response = new AuthResponse("mock.access.token", "mock.refresh.token", 900000L, userSummary);

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock.access.token"))
                .andExpect(jsonPath("$.data.refreshToken").value("mock.refresh.token"))
                .andExpect(jsonPath("$.data.user.username").value("johndoe"));
    }

    @Test
    @DisplayName("POST /api/auth/login returns 400 when body fields are missing")
    void testLogin_ValidationError() throws Exception {
        LoginRequest request = new LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("POST /api/auth/refresh returns 200 with new rotated tokens")
    void testRefreshToken_Success() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        TokenRefreshResponse response = new TokenRefreshResponse("new.access.token", "new.refresh.token", 900000L);

        when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new.access.token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new.refresh.token"));
    }

    @Test
    @DisplayName("POST /api/auth/logout returns 200")
    void testLogout_Success() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        doNothing().when(authService).logout("valid-refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    @DisplayName("POST /api/auth/verify returns 200 upon successful verification")
    void testVerifyEmailPost_Success() throws Exception {
        VerifyEmailRequest request = new VerifyEmailRequest("valid-verification-token");
        doNothing().when(authService).verifyEmail("valid-verification-token");

        mockMvc.perform(post("/api/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/auth/verify?token=... returns 200 upon clicking link")
    void testVerifyEmailGet_Success() throws Exception {
        doNothing().when(authService).verifyEmail("valid-verification-token");

        mockMvc.perform(get("/api/auth/verify")
                        .param("token", "valid-verification-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
