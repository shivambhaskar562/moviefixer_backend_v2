package com.moviefixer.show.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class LockSeatsRequest {

    @NotEmpty(message = "At least one seat must be selected to lock")
    @Size(max = 10, message = "A customer cannot lock more than 10 seats in a single reservation")
    private List<UUID> seatIds;

    public LockSeatsRequest() {
    }

    public LockSeatsRequest(List<UUID> seatIds) {
        this.seatIds = seatIds;
    }

    public List<UUID> getSeatIds() {
        return seatIds;
    }

    public void setSeatIds(List<UUID> seatIds) {
        this.seatIds = seatIds;
    }
}
