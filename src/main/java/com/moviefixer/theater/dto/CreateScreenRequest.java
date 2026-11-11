package com.moviefixer.theater.dto;

import com.moviefixer.theater.enums.ScreenType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateScreenRequest {

    @NotBlank(message = "Screen name is required (e.g., Screen 1, IMAX Hall)")
    @Size(min = 1, max = 100, message = "Screen name must be between 1 and 100 characters")
    private String name;

    @NotNull(message = "Screen type / projection format is required")
    private ScreenType screenType;

    @Size(max = 100, message = "Sound system description must not exceed 100 characters")
    private String soundSystem;

    @Min(value = 0, message = "Total seats cannot be negative")
    private Integer totalSeats;

    public CreateScreenRequest() {
    }

    public CreateScreenRequest(String name, ScreenType screenType, String soundSystem, Integer totalSeats) {
        this.name = name;
        this.screenType = screenType;
        this.soundSystem = soundSystem;
        this.totalSeats = totalSeats;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ScreenType getScreenType() {
        return screenType;
    }

    public void setScreenType(ScreenType screenType) {
        this.screenType = screenType;
    }

    public String getSoundSystem() {
        return soundSystem;
    }

    public void setSoundSystem(String soundSystem) {
        this.soundSystem = soundSystem;
    }

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }
}
