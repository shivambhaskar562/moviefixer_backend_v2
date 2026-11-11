package com.moviefixer.show.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class CreateShowRequest {

    @NotNull(message = "Movie ID is required")
    private UUID movieId;

    @NotNull(message = "Screen ID is required")
    private UUID screenId;

    @NotNull(message = "Start time is required")
    @Future(message = "Show start time must be in the future")
    private LocalDateTime startTime;

    @NotNull(message = "Base ticket price is required")
    @DecimalMin(value = "1.0", message = "Base price must be at least 1.0")
    @DecimalMax(value = "10000.0", message = "Base price cannot exceed 10000.0")
    private BigDecimal basePrice;

    private Integer trailerMinutes = 10; // Extra trailers/previews time before main film

    public CreateShowRequest() {
    }

    public CreateShowRequest(UUID movieId, UUID screenId, LocalDateTime startTime, BigDecimal basePrice, Integer trailerMinutes) {
        this.movieId = movieId;
        this.screenId = screenId;
        this.startTime = startTime;
        this.basePrice = basePrice;
        this.trailerMinutes = (trailerMinutes != null) ? trailerMinutes : 10;
    }

    public UUID getMovieId() {
        return movieId;
    }

    public void setMovieId(UUID movieId) {
        this.movieId = movieId;
    }

    public UUID getScreenId() {
        return screenId;
    }

    public void setScreenId(UUID screenId) {
        this.screenId = screenId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public Integer getTrailerMinutes() {
        return trailerMinutes;
    }

    public void setTrailerMinutes(Integer trailerMinutes) {
        this.trailerMinutes = trailerMinutes;
    }
}
