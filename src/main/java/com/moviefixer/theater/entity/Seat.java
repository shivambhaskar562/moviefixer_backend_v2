package com.moviefixer.theater.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seats_screen_row_num", columnNames = {"screen_id", "row_identifier", "seat_number"}),
                @UniqueConstraint(name = "uk_seats_screen_grid", columnNames = {"screen_id", "grid_row", "grid_column"})
        },
        indexes = {
                @Index(name = "idx_seats_screen_id", columnList = "screen_id"),
                @Index(name = "idx_seats_tier_id", columnList = "tier_id"),
                @Index(name = "idx_seats_grid", columnList = "screen_id, grid_row, grid_column")
        }
)
public class Seat {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(name = "row_identifier", nullable = false, length = 10)
    private String rowIdentifier;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tier_id", nullable = false)
    private SeatingTier tier;

    @Column(name = "grid_row", nullable = false)
    private Integer gridRow;

    @Column(name = "grid_column", nullable = false)
    private Integer gridColumn;

    @Column(name = "is_aisle", nullable = false)
    private boolean isAisle = false;

    @Column(name = "is_wheelchair_accessible", nullable = false)
    private boolean isWheelchairAccessible = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Seat() {
    }

    public Seat(Screen screen, String rowIdentifier, Integer seatNumber, SeatingTier tier,
                Integer gridRow, Integer gridColumn, boolean isAisle, boolean isWheelchairAccessible) {
        this.screen = screen;
        this.rowIdentifier = rowIdentifier;
        this.seatNumber = seatNumber;
        this.tier = tier;
        this.gridRow = gridRow;
        this.gridColumn = gridColumn;
        this.isAisle = isAisle;
        this.isWheelchairAccessible = isWheelchairAccessible;
        this.active = true;
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

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public String getRowIdentifier() {
        return rowIdentifier;
    }

    public void setRowIdentifier(String rowIdentifier) {
        this.rowIdentifier = rowIdentifier;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatingTier getTier() {
        return tier;
    }

    public void setTier(SeatingTier tier) {
        this.tier = tier;
    }

    public Integer getGridRow() {
        return gridRow;
    }

    public void setGridRow(Integer gridRow) {
        this.gridRow = gridRow;
    }

    public Integer getGridColumn() {
        return gridColumn;
    }

    public void setGridColumn(Integer gridColumn) {
        this.gridColumn = gridColumn;
    }

    public boolean isAisle() {
        return isAisle;
    }

    public void setAisle(boolean aisle) {
        isAisle = aisle;
    }

    public boolean isWheelchairAccessible() {
        return isWheelchairAccessible;
    }

    public void setWheelchairAccessible(boolean wheelchairAccessible) {
        isWheelchairAccessible = wheelchairAccessible;
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
        Seat seat = (Seat) o;
        return Objects.equals(id, seat.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
