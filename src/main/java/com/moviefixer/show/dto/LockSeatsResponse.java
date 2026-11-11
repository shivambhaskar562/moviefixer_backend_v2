package com.moviefixer.show.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LockSeatsResponse {

    private UUID showId;
    private List<UUID> lockedSeatIds;
    private LocalDateTime lockedUntil;
    private long expiresInSeconds;
    private BigDecimal totalPrice;
    private List<ShowSeatResponse> seats;

    public LockSeatsResponse() {
    }

    public LockSeatsResponse(UUID showId, List<UUID> lockedSeatIds, LocalDateTime lockedUntil,
                             long expiresInSeconds, BigDecimal totalPrice, List<ShowSeatResponse> seats) {
        this.showId = showId;
        this.lockedSeatIds = lockedSeatIds;
        this.lockedUntil = lockedUntil;
        this.expiresInSeconds = expiresInSeconds;
        this.totalPrice = totalPrice;
        this.seats = seats;
    }

    public UUID getShowId() {
        return showId;
    }

    public void setShowId(UUID showId) {
        this.showId = showId;
    }

    public List<UUID> getLockedSeatIds() {
        return lockedSeatIds;
    }

    public void setLockedSeatIds(List<UUID> lockedSeatIds) {
        this.lockedSeatIds = lockedSeatIds;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public List<ShowSeatResponse> getSeats() {
        return seats;
    }

    public void setSeats(List<ShowSeatResponse> seats) {
        this.seats = seats;
    }
}
