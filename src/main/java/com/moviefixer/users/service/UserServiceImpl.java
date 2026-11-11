package com.moviefixer.users.service;

import com.moviefixer.auth.security.PasswordHashProvider;
import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.location.dto.CitySummaryResponse;
import com.moviefixer.location.entity.City;
import com.moviefixer.location.repository.CityRepository;
import com.moviefixer.users.dto.CreateUserRequest;
import com.moviefixer.users.dto.RegisterUserRequest;
import com.moviefixer.users.dto.UserResponse;
import com.moviefixer.users.entity.Role;
import com.moviefixer.users.entity.Users;
import com.moviefixer.users.enums.AccountStatus;
import com.moviefixer.users.enums.USER_ROLE;
import com.moviefixer.users.exception.UserAlreadyExistsException;
import com.moviefixer.users.event.UserRegisteredEvent;
import com.moviefixer.users.repository.RoleRepository;
import com.moviefixer.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CityRepository cityRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PasswordHashProvider passwordHashProvider;

    public UserServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            CityRepository cityRepository,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher eventPublisher,
            PasswordHashProvider passwordHashProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.cityRepository = cityRepository;
        this.eventPublisher = eventPublisher;
        this.passwordHashProvider = passwordHashProvider;
    }

    @Override
    public UserResponse registerCustomer(RegisterUserRequest request) {
        log.info("Processing public registration for username: {}, email: {}", request.getUsername(), request.getEmail());

        // 1. Validate uniqueness
        validateUniqueConstraints(request.getUsername(), request.getEmail(), request.getMobile());

        // 2. Fetch and assign default CUSTOMER role on the server
        Role customerRole = roleRepository.findByName(USER_ROLE.CUSTOMER)
                .orElseThrow(() -> new ResourceNotFoundException("Default role CUSTOMER not found in database. Please verify role seed data."));

        // 3. Resolve preferred city if provided
        City preferredCity = null;
        if (request.getPreferredCityId() != null) {
            preferredCity = cityRepository.findById(request.getPreferredCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Preferred city not found with id: " + request.getPreferredCityId()));
        }

        // 4. Construct entity
        Users user = new Users();
        user.setName(request.getName().trim());
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setMobile(request.getMobile().trim());

        // 5. Hash password with BCrypt
        user.setPasswordHash(passwordHashProvider.hash(request.getPassword()));

        // 6. Set initial account status to PENDING
        user.setAccountStatus(AccountStatus.PENDING);

        // 7. Assign CUSTOMER role
        user.addRole(customerRole);

        // 8. Set optional profile details
        user.setPreferredCity(preferredCity);
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setProfileImageUrl(request.getProfileImageUrl());

        // 9. Persist user
        Users savedUser = userRepository.save(user);
        log.info("Successfully registered customer with ID: {} and username: {}", savedUser.getId(), savedUser.getUsername());

        // 10. Publish domain event to trigger verification token generation

        eventPublisher.publishEvent(new UserRegisteredEvent(savedUser));


        return mapToUserResponse(savedUser);
    }

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Processing admin user creation for username: {}", request.getUsername());

        // 1. Validate uniqueness
        validateUniqueConstraints(request.getUsername(), request.getEmail(), request.getMobile());

        // 2. Resolve roles
        Set<Role> assignedRoles = new HashSet<>();
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            for (USER_ROLE roleEnum : request.getRoles()) {
                Role role = roleRepository.findByName(roleEnum)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleEnum));
                assignedRoles.add(role);
            }
        } else {
            Role defaultCustomer = roleRepository.findByName(USER_ROLE.CUSTOMER)
                    .orElseThrow(() -> new ResourceNotFoundException("Default role CUSTOMER not found"));
            assignedRoles.add(defaultCustomer);
        }

        // 3. Resolve preferred city if provided
        City preferredCity = null;
        if (request.getPreferredCityId() != null) {
            preferredCity = cityRepository.findById(request.getPreferredCityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Preferred city not found with id: " + request.getPreferredCityId()));
        }

        // 4. Construct entity
        Users user = new Users();
        user.setName(request.getName().trim());
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setMobile(request.getMobile().trim());
        user.setPasswordHash(passwordHashProvider.hash(request.getPassword()));
        user.setAccountStatus(AccountStatus.PENDING);
        user.setRoles(assignedRoles);
        user.setPreferredCity(preferredCity);
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setProfileImageUrl(request.getProfileImageUrl());

        Users savedUser = userRepository.save(user);
        eventPublisher.publishEvent(new UserRegisteredEvent(user));
        return mapToUserResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByUsername(String username) {
        Users user = userRepository.findByUsername(username.trim().toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToUserResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return mapToUserResponse(user);
    }

    private void validateUniqueConstraints(String username, String email, String mobile) {
        String cleanUsername = username.trim().toLowerCase();
        if (userRepository.existsByUsername(cleanUsername)) {
            throw new UserAlreadyExistsException("Username is already taken: " + username);
        }

        String cleanEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(cleanEmail)) {
            throw new UserAlreadyExistsException("Email is already registered: " + email);
        }

        String cleanMobile = mobile.trim();
        if (userRepository.existsByMobile(cleanMobile)) {
            throw new UserAlreadyExistsException("Mobile number is already registered: " + mobile);
        }
    }

    private UserResponse mapToUserResponse(Users user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setMobile(user.getMobile());
        response.setAccountStatus(user.getAccountStatus());
        response.setDateOfBirth(user.getDateOfBirth());
        response.setGender(user.getGender());
        response.setProfileImageUrl(user.getProfileImageUrl());
        response.setEmailVerified(user.getEmailVerifiedAt() != null);
        response.setMobileVerified(user.getMobileVerifiedAt() != null);
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        // Map roles
        if (user.getRoles() != null) {
            Set<String> roleNames = user.getRoles().stream()
                    .map(r -> r.getName().name())
                    .collect(Collectors.toSet());
            response.setRoles(roleNames);
        }

        // Map preferred city
        if (user.getPreferredCity() != null) {
            City city = user.getPreferredCity();
            CitySummaryResponse cityResponse = new CitySummaryResponse();
            cityResponse.setId(city.getId());
            cityResponse.setName(city.getName());
            if (city.getState() != null) {
                cityResponse.setStateName(city.getState().getName());
                if (city.getState().getCountry() != null) {
                    cityResponse.setCountryName(city.getState().getCountry().getName());
                    cityResponse.setCountryCode(city.getState().getCountry().getCode());
                }
            }
            response.setPreferredCity(cityResponse);
        }

        return response;
    }
}
