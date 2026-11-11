package com.moviefixer.show.dto;

import com.moviefixer.show.enums.ShowStatus;
import com.moviefixer.theater.enums.ScreenType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ShowSummaryResponse {

    private UUID id;
    private UUID screenId;
    private String screenName;
    private ScreenType screenType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal basePrice;
    private ShowStatus status;
    private long availableSeats;

    public ShowSummaryResponse() {
    }

    public ShowSummaryResponse(UUID id, UUID screenId, String screenName, ScreenType screenType,
                               LocalDateTime startTime, LocalDateTime endTime, BigDecimal basePrice,
                               ShowStatus status, long availableSeats) {
        this.id = id;
        this.screenId = screenId;
        this.screenName = screenName;
        this.screenType = screenType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.basePrice = basePrice;
        this.status = status;
        this.availableSeats = availableSeats;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getScreenId() {
        return screenId;
    }

    public void setScreenId(UUID screenId) {
        this.screenId = screenId;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public ScreenType getScreenType() {
        return screenType;
    }

    public void setScreenType(ScreenType screenType) {
        this.screenType = screenType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public ShowStatus getStatus() {
        return status;
    }

    public void setStatus(ShowStatus status) {
        this.status = status;
    }

    public long getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(long availableSeats) {
        this.availableSeats = availableSeats;
    }
}
