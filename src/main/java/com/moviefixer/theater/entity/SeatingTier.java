package com.moviefixer.theater.entity;

import com.moviefixer.theater.enums.TierType;
import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "seating_tiers",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seating_tiers_name", columnNames = {"name"}),
                @UniqueConstraint(name = "uk_seating_tiers_type", columnNames = {"tier_type"})
        }
)
public class SeatingTier {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;


    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_type", nullable = false, length = 30)
    private TierType tierType;

    @Column(name = "default_multiplier", nullable = false, precision = 5, scale = 2)
    private BigDecimal defaultMultiplier;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public SeatingTier() {
    }

    public SeatingTier(String name, TierType tierType, BigDecimal defaultMultiplier, String description) {
        this(name, tierType, defaultMultiplier, description, true);
    }

    public SeatingTier(String name, TierType tierType, BigDecimal defaultMultiplier, String description, boolean active) {
        this.name = name;
        this.tierType = tierType;
        this.defaultMultiplier = defaultMultiplier;
        this.description = description;
        this.active = active;
    }


    public SeatingTier(UUID id, String name, TierType tierType, BigDecimal defaultMultiplier, String description, boolean active) {
        this.id = id;
        this.name = name;
        this.tierType = tierType;
        this.defaultMultiplier = defaultMultiplier;
        this.description = description;
        this.active = active;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeatingTier that = (SeatingTier) o;
        return Objects.equals(id, that.id) || tierType == that.tierType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, tierType);
    }
}
