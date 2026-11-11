package com.moviefixer.theater.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class BatchSeatLayoutRequest {

    @NotEmpty(message = "Seats list cannot be empty when configuring layout")
    @Valid
    private List<SeatItemDto> seats;

    public BatchSeatLayoutRequest() {
    }

    public BatchSeatLayoutRequest(List<SeatItemDto> seats) {
        this.seats = seats;
    }

    public List<SeatItemDto> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatItemDto> seats) {
        this.seats = seats;
    }
}
