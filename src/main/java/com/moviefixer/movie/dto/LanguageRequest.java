package com.moviefixer.movie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LanguageRequest {

    @NotBlank(message = "Language name is required")
    @Size(min = 2, max = 100, message = "Language name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Language code is required (e.g. 'en', 'hi', 'te')")
    @Size(min = 2, max = 10, message = "Language code must be between 2 and 10 characters")
    private String code;

    private boolean active = true;

    public LanguageRequest() {
    }

    public LanguageRequest(String name, String code, boolean active) {
        this.name = name;
        this.code = code;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
