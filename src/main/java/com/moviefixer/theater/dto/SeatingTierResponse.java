package com.moviefixer.theater.dto;

import com.moviefixer.theater.enums.TierType;

import java.math.BigDecimal;
import java.util.UUID;

public class SeatingTierResponse {

    private UUID id;
    private String name;
    private TierType tierType;
    private BigDecimal defaultMultiplier;
    private String description;
    private boolean active;

    public SeatingTierResponse() {
    }

    public SeatingTierResponse(UUID id, String name, TierType tierType, BigDecimal defaultMultiplier, String description, boolean active) {
        this.id = id;
        this.name = name;
        this.tierType = tierType;
        this.defaultMultiplier = defaultMultiplier;
        this.description = description;
        this.active = active;
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

    public TierType getTierType() {
        return tierType;
    }

    public void setTierType(TierType tierType) {
        this.tierType = tierType;
    }

    public BigDecimal getDefaultMultiplier() {
        return defaultMultiplier;
    }

    public void setDefaultMultiplier(BigDecimal defaultMultiplier) {
        this.defaultMultiplier = defaultMultiplier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
