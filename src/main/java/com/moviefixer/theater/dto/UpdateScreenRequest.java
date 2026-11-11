package com.moviefixer.theater.dto;

import com.moviefixer.theater.enums.ScreenType;
import jakarta.validation.constraints.Size;

public class UpdateScreenRequest {

    @Size(min = 1, max = 100, message = "Screen name must be between 1 and 100 characters")
    private String name;

    private ScreenType screenType;

    @Size(max = 100, message = "Sound system description must not exceed 100 characters")
    private String soundSystem;

    private Boolean active;

    public UpdateScreenRequest() {
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
