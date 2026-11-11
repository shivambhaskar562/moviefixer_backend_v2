package com.moviefixer.users.dto;

import com.moviefixer.users.enums.Gender;
import com.moviefixer.users.enums.USER_ROLE;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Admin Create User Request Payload")
public class CreateUserRequest {

    @Schema(description = "User's full name", example = "Jane Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Schema(description = "Unique username", example = "janedoe", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username can only contain alphanumeric characters, dots, underscores, and hyphens")
    private String username;

    @Schema(description = "Valid email address", example = "jane.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Schema(description = "Mobile number", example = "+919876543211", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Mobile number must be between 10 and 15 digits, optionally starting with '+'")
    private String mobile;

    @Schema(description = "Password", example = "P@ssword123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    @Schema(description = "Optional roles assigned to user. Defaults to CUSTOMER if empty.", example = "[\"THEATER_MANAGER\"]")
    private Set<USER_ROLE> roles;

    @Schema(description = "Optional preferred city UUID", example = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private java.util.UUID preferredCityId;

    @Schema(description = "Optional date of birth", example = "1992-08-20")
    @Past(message = "Date of birth must be a past date")
    private LocalDate dateOfBirth;

    @Schema(description = "Optional gender", example = "FEMALE")
    private Gender gender;

    @Schema(description = "Optional profile image URL", example = "https://example.com/jane.jpg")
    @Size(max = 500, message = "Profile image URL cannot exceed 500 characters")
    private String profileImageUrl;

    public CreateUserRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<USER_ROLE> getRoles() {
        return roles;
    }

    public void setRoles(Set<USER_ROLE> roles) {
        this.roles = roles;
    }

    public java.util.UUID getPreferredCityId() {
        return preferredCityId;
    }

    public void setPreferredCityId(java.util.UUID preferredCityId) {
        this.preferredCityId = preferredCityId;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
