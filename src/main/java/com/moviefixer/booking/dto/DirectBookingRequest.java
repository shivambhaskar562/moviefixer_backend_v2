package com.moviefixer.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class DirectBookingRequest {

    @NotNull(message = "Show ID is required")
    private UUID showId;

    @NotEmpty(message = "At least one seat must be selected")
    @Size(max = 10, message = "Cannot book more than 10 seats in a single reservation")
    private List<UUID> seatIds;

    public DirectBookingRequest() {
    }

    public DirectBookingRequest(UUID showId, List<UUID> seatIds) {
        this.showId = showId;
        this.seatIds = seatIds;
    }

    public UUID getShowId() {
        return showId;
    }

    public void setShowId(UUID showId) {
        this.showId = showId;
    }

    public List<UUID> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<UUID> seatIds) {
        this.seatIds = seatIds;
    }
}
