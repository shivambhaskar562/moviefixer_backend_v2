package com.moviefixer.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.moviefixer.common.exception.GlobalExceptionHandler;
import com.moviefixer.users.dto.RegisterUserRequest;
import com.moviefixer.users.dto.UserResponse;
import com.moviefixer.users.enums.AccountStatus;
import com.moviefixer.users.exception.UserAlreadyExistsException;
import com.moviefixer.users.service.UserService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CreateUserControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private UserService userService;

    @InjectMocks
    private CreateUserController createUserController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(createUserController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/user/register returns 201 when payload is valid")
    void testRegisterUser_Success() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("Alice Smith");
        request.setUsername("alicesmith");
        request.setEmail("alice@example.com");
        request.setMobile("+919876543210");
        request.setPassword("SecureP@ss123");

        UserResponse mockResponse = new UserResponse();
        mockResponse.setId(UUID.randomUUID());
        mockResponse.setName("Alice Smith");
        mockResponse.setUsername("alicesmith");
        mockResponse.setEmail("alice@example.com");
        mockResponse.setMobile("+919876543210");
        mockResponse.setAccountStatus(AccountStatus.PENDING);
        mockResponse.setRoles(Set.of("CUSTOMER"));

        when(userService.registerCustomer(any(RegisterUserRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.username").value("alicesmith"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.accountStatus").value("PENDING"))
                .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/user/register returns 400 when required fields are missing")
    void testRegisterUser_ValidationError() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        // Missing name, username, email, mobile, password

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.mobile").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("POST /api/user/register returns 409 when user already exists")
    void testRegisterUser_DuplicateConflict() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("Alice Smith");
        request.setUsername("alicesmith");
        request.setEmail("alice@example.com");
        request.setMobile("+919876543210");
        request.setPassword("SecureP@ss123");

        when(userService.registerCustomer(any(RegisterUserRequest.class)))
                .thenThrow(new UserAlreadyExistsException("Username is already taken: alicesmith"));

        mockMvc.perform(post("/api/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Username is already taken: alicesmith"));
    }
}
