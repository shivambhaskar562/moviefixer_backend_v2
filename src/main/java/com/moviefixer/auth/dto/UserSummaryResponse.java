package com.moviefixer.auth.dto;

import com.moviefixer.users.enums.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;
import java.util.UUID;

public class UserSummaryResponse {

    @Schema(description = "User unique UUID identifier")
    private UUID id;

    @Schema(description = "Full display name")
    private String name;

    @Schema(description = "Unique username")
    private String username;

    @Schema(description = "Registered email address")
    private String email;

    @Schema(description = "Registered mobile number")
    private String mobile;

    @Schema(description = "Account verification/lifecycle status")
    private AccountStatus accountStatus;

    @Schema(description = "Assigned security roles")
    private Set<String> roles;

    public UserSummaryResponse() {
    }

    public UserSummaryResponse(UUID id, String name, String username, String email, String mobile, AccountStatus accountStatus, Set<String> roles) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.email = email;
        this.mobile = mobile;
        this.accountStatus = accountStatus;
        this.roles = roles;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(AccountStatus accountStatus) {
        this.accountStatus = accountStatus;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
