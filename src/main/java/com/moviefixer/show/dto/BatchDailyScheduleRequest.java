package com.moviefixer.show.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class BatchDailyScheduleRequest {

    @NotNull(message = "Movie ID is required")
    private UUID movieId;

    @NotNull(message = "Screen ID is required")
    private UUID screenId;

    @NotNull(message = "Date of screening is required")
    private LocalDate date;

    @NotNull(message = "First show start time is required (e.g. 09:30)")
    private LocalTime firstShowTime;

    @NotNull(message = "Number of shows is required (e.g. 5 or 6 times a day)")
    @Min(value = 1, message = "At least 1 show must be scheduled")
    @Max(value = 10, message = "Cannot schedule more than 10 shows in a single day on one screen")
    private Integer numberOfShows = 5;

    @Min(value = 15, message = "Turnaround buffer must be at least 15 minutes")
    @Max(value = 120, message = "Turnaround buffer cannot exceed 120 minutes")
    private Integer turnaroundMinutes = 30; // 30 min cleaning & air cycling buffer between shows

    private Integer trailerMinutes = 10; // Extra trailers/previews time before main film

    @NotNull(message = "Base ticket price is required")
    @DecimalMin(value = "1.0", message = "Base price must be at least 1.0")
    @DecimalMax(value = "10000.0", message = "Base price cannot exceed 10000.0")
    private BigDecimal basePrice;

    public BatchDailyScheduleRequest() {
    }

    public BatchDailyScheduleRequest(UUID movieId, UUID screenId, LocalDate date, LocalTime firstShowTime,
                                     Integer numberOfShows, Integer turnaroundMinutes, Integer trailerMinutes,
                                     BigDecimal basePrice) {
        this.movieId = movieId;
        this.screenId = screenId;
        this.date = date;
        this.firstShowTime = firstShowTime;
        this.numberOfShows = (numberOfShows != null) ? numberOfShows : 5;
        this.turnaroundMinutes = (turnaroundMinutes != null) ? turnaroundMinutes : 30;
        this.trailerMinutes = (trailerMinutes != null) ? trailerMinutes : 10;
        this.basePrice = basePrice;
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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getFirstShowTime() {
        return firstShowTime;
    }

    public void setFirstShowTime(LocalTime firstShowTime) {
        this.firstShowTime = firstShowTime;
    }

    public Integer getNumberOfShows() {
        return numberOfShows;
    }

    public void setNumberOfShows(Integer numberOfShows) {
        this.numberOfShows = numberOfShows;
    }

    public Integer getTurnaroundMinutes() {
        return turnaroundMinutes;
    }

    public void setTurnaroundMinutes(Integer turnaroundMinutes) {
        this.turnaroundMinutes = turnaroundMinutes;
    }

    public Integer getTrailerMinutes() {
        return trailerMinutes;
    }

    public void setTrailerMinutes(Integer trailerMinutes) {
        this.trailerMinutes = trailerMinutes;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
}
