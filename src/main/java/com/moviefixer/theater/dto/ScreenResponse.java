package com.moviefixer.theater.dto;

import com.moviefixer.theater.enums.ScreenType;

import java.time.Instant;
import java.util.UUID;

public class ScreenResponse {

    private UUID id;
    private UUID theaterId;
    private String theaterName;
    private String name;
    private ScreenType screenType;
    private String soundSystem;
    private Integer totalSeats;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;

    public ScreenResponse() {
    }

    public ScreenResponse(UUID id, UUID theaterId, String theaterName, String name, ScreenType screenType,
                          String soundSystem, Integer totalSeats, boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.theaterId = theaterId;
        this.theaterName = theaterName;
        this.name = name;
        this.screenType = screenType;
        this.soundSystem = soundSystem;
        this.totalSeats = totalSeats;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTheaterId() {
        return theaterId;
    }

    public void setTheaterId(UUID theaterId) {
        this.theaterId = theaterId;
    }

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
