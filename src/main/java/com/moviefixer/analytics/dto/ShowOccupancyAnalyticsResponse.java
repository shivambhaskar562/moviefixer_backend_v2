package com.moviefixer.analytics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ShowOccupancyAnalyticsResponse {

    private UUID showId;
    private String movieTitle;
    private String theaterName;
    private String screenName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private long totalCapacity;
    private long bookedSeatsCount;
    private long availableSeatsCount;
    private long lockedSeatsCount;
    private double occupancyPercentage;
    private BigDecimal grossRevenue;
    private List<TierBreakdownResponse> tierBreakdowns;

    public ShowOccupancyAnalyticsResponse() {
    }

    public ShowOccupancyAnalyticsResponse(UUID showId, String movieTitle, String theaterName,
                                          String screenName, LocalDateTime startTime,
                                          LocalDateTime endTime, String status,
                                          long totalCapacity, long bookedSeatsCount,
                                          long availableSeatsCount, long lockedSeatsCount,
                                          double occupancyPercentage, BigDecimal grossRevenue,
                                          List<TierBreakdownResponse> tierBreakdowns) {
        this.showId = showId;
        this.movieTitle = movieTitle;
        this.theaterName = theaterName;
        this.screenName = screenName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.totalCapacity = totalCapacity;
        this.bookedSeatsCount = bookedSeatsCount;
        this.availableSeatsCount = availableSeatsCount;
        this.lockedSeatsCount = lockedSeatsCount;
        this.occupancyPercentage = occupancyPercentage;
        this.grossRevenue = grossRevenue;
        this.tierBreakdowns = tierBreakdowns;
    }

    public UUID getShowId() {
        return showId;
    }

    public void setShowId(UUID showId) {
        this.showId = showId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTotalCapacity() {
        return totalCapacity;
    }

    public void setTotalCapacity(long totalCapacity) {
        this.totalCapacity = totalCapacity;
    }

    public long getBookedSeatsCount() {
        return bookedSeatsCount;
    }

    public void setBookedSeatsCount(long bookedSeatsCount) {
        this.bookedSeatsCount = bookedSeatsCount;
    }

    public long getAvailableSeatsCount() {
        return availableSeatsCount;
    }

    public void setAvailableSeatsCount(long availableSeatsCount) {
        this.availableSeatsCount = availableSeatsCount;
    }

    public long getLockedSeatsCount() {
        return lockedSeatsCount;
    }

    public void setLockedSeatsCount(long lockedSeatsCount) {
        this.lockedSeatsCount = lockedSeatsCount;
    }

    public double getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public void setOccupancyPercentage(double occupancyPercentage) {
        this.occupancyPercentage = occupancyPercentage;
    }

    public BigDecimal getGrossRevenue() {
        return grossRevenue;
    }

    public void setGrossRevenue(BigDecimal grossRevenue) {
        this.grossRevenue = grossRevenue;
    }

    public List<TierBreakdownResponse> getTierBreakdowns() {
        return tierBreakdowns;
    }

    public void setTierBreakdowns(List<TierBreakdownResponse> tierBreakdowns) {
        this.tierBreakdowns = tierBreakdowns;
    }
}
