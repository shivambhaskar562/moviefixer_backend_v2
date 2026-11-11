package com.moviefixer.theater.dto;

import com.moviefixer.theater.enums.TierType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class SeatingTierRequest {

    @NotBlank(message = "Tier name is required")
    @Size(min = 2, max = 50, message = "Tier name must be between 2 and 50 characters")
    private String name;

    @NotNull(message = "Tier type is required")
    private TierType tierType;

    @NotNull(message = "Default multiplier is required")
    @DecimalMin(value = "0.5", message = "Multiplier must be at least 0.5")
    @DecimalMax(value = "10.0", message = "Multiplier cannot exceed 10.0")
    private BigDecimal defaultMultiplier;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private Boolean active = true;

    public SeatingTierRequest() {
    }

    public SeatingTierRequest(String name, TierType tierType, BigDecimal defaultMultiplier, String description, Boolean active) {
        this.name = name;
        this.tierType = tierType;
        this.defaultMultiplier = defaultMultiplier;
        this.description = description;
        this.active = (active != null) ? active : true;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
