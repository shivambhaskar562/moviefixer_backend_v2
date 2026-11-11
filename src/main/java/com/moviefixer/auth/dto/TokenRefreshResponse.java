package com.moviefixer.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class TokenRefreshResponse {

    @Schema(description = "New JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "New rotated Refresh Token", example = "8a32d1ef-42f0-410a-b328-98e8d89025fa")
    private String refreshToken;

    @Schema(description = "Token type prefix", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Access token validity in milliseconds", example = "900000")
    private long expiresIn;

    public TokenRefreshResponse() {
    }

    public TokenRefreshResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = "Bearer";
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}
