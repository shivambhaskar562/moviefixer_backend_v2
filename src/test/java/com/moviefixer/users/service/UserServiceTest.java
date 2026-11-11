package com.moviefixer.users.service;

import com.moviefixer.location.entity.City;
import com.moviefixer.location.entity.Country;
import com.moviefixer.location.entity.State;
import com.moviefixer.location.repository.CityRepository;
import com.moviefixer.users.dto.RegisterUserRequest;
import com.moviefixer.users.dto.UserResponse;
import com.moviefixer.users.entity.Role;
import com.moviefixer.users.entity.Users;
import com.moviefixer.users.enums.AccountStatus;
import com.moviefixer.users.enums.Gender;
import com.moviefixer.users.enums.USER_ROLE;
import com.moviefixer.users.exception.UserAlreadyExistsException;
import com.moviefixer.users.repository.RoleRepository;
import com.moviefixer.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    private Role customerRole;
    private City mumbaiCity;
    private UUID mumbaiCityId;

    @BeforeEach
    void setUp() {
        customerRole = new Role(USER_ROLE.CUSTOMER, "Standard customer");
        customerRole.setId(UUID.randomUUID());

        Country india = new Country("IN", "India", "+91");
        india.setId(UUID.randomUUID());

        State maharashtra = new State("Maharashtra", "MH", india);
        maharashtra.setId(UUID.randomUUID());

        mumbaiCityId = UUID.randomUUID();
        mumbaiCity = new City("Mumbai", maharashtra);
        mumbaiCity.setId(mumbaiCityId);
    }

    @Test
    @DisplayName("Should successfully register customer with CUSTOMER role and PENDING status")
    void testRegisterCustomer_Success() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("John Doe");
        request.setUsername("johndoe");
        request.setEmail("john.doe@example.com");
        request.setMobile("+919876543210");
        request.setPassword("PlainPassword123!");
        request.setPreferredCityId(mumbaiCityId);
        request.setGender(Gender.MALE);
        request.setDateOfBirth(LocalDate.of(1995, 5, 15));

        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(userRepository.existsByMobile("+919876543210")).thenReturn(false);
        when(roleRepository.findByName(USER_ROLE.CUSTOMER)).thenReturn(Optional.of(customerRole));
        when(cityRepository.findById(mumbaiCityId)).thenReturn(Optional.of(mumbaiCity));
        when(passwordEncoder.encode("PlainPassword123!")).thenReturn("$2a$10$hashedPasswordBcrypt");

        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> {
            Users saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        UserResponse response = userService.registerCustomer(request);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("johndoe", response.getUsername());
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals("+919876543210", response.getMobile());
        assertEquals(AccountStatus.PENDING, response.getAccountStatus());
        assertTrue(response.getRoles().contains("CUSTOMER"));
        assertNotNull(response.getPreferredCity());
        assertEquals(mumbaiCityId, response.getPreferredCity().getId());
        assertEquals("Mumbai", response.getPreferredCity().getName());
        assertEquals("Maharashtra", response.getPreferredCity().getStateName());
        assertEquals("India", response.getPreferredCity().getCountryName());
        assertFalse(response.isEmailVerified());
        assertFalse(response.isMobileVerified());

        // Verify password hash argument
        ArgumentCaptor<Users> captor = ArgumentCaptor.forClass(Users.class);
        verify(userRepository).save(captor.capture());
        assertEquals("$2a$10$hashedPasswordBcrypt", captor.getValue().getPasswordHash());
    }

    @Test
    @DisplayName("Should throw exception when username already exists")
    void testRegisterCustomer_DuplicateUsername() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("John Doe");
        request.setUsername("johndoe");
        request.setEmail("john.doe@example.com");
        request.setMobile("+919876543210");
        request.setPassword("PlainPassword123!");

        when(userRepository.existsByUsername("johndoe")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerCustomer(request)
        );

        assertTrue(exception.getMessage().contains("Username is already taken"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegisterCustomer_DuplicateEmail() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("John Doe");
        request.setUsername("johndoe");
        request.setEmail("john.doe@example.com");
        request.setMobile("+919876543210");
        request.setPassword("PlainPassword123!");

        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerCustomer(request)
        );

        assertTrue(exception.getMessage().contains("Email is already registered"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when mobile already exists")
    void testRegisterCustomer_DuplicateMobile() {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setName("John Doe");
        request.setUsername("johndoe");
        request.setEmail("john.doe@example.com");
        request.setMobile("+919876543210");
        request.setPassword("PlainPassword123!");

        when(userRepository.existsByUsername("johndoe")).thenReturn(false);
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(false);
        when(userRepository.existsByMobile("+919876543210")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerCustomer(request)
        );

        assertTrue(exception.getMessage().contains("Mobile number is already registered"));
        verify(userRepository, never()).save(any());
    }
}
