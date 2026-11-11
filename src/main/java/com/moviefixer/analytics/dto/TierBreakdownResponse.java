package com.moviefixer.analytics.dto;

import java.math.BigDecimal;

public class TierBreakdownResponse {

    private String tierName;
    private String tierType;
    private BigDecimal price;
    private long totalSeats;
    private long bookedSeats;
    private double occupancyPercentage;
    private BigDecimal revenue;

    public TierBreakdownResponse() {
    }

    public TierBreakdownResponse(String tierName, String tierType, BigDecimal price,
                                 long totalSeats, long bookedSeats,
                                 double occupancyPercentage, BigDecimal revenue) {
        this.tierName = tierName;
        this.tierType = tierType;
        this.price = price;
        this.totalSeats = totalSeats;
        this.bookedSeats = bookedSeats;
        this.occupancyPercentage = occupancyPercentage;
        this.revenue = revenue;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public String getTierType() {
        return tierType;
    }

    public void setTierType(String tierType) {
        this.tierType = tierType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public long getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(long totalSeats) {
        this.totalSeats = totalSeats;
    }

    public long getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(long bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public double getOccupancyPercentage() {
        return occupancyPercentage;
    }

    public void setOccupancyPercentage(double occupancyPercentage) {
        this.occupancyPercentage = occupancyPercentage;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}
