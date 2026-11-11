package com.moviefixer.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class VendorTheaterAnalyticsResponse {

    private UUID theaterId;
    private String theaterName;
    private String cityName;
    private long screensCount;
    private long showsCount;
    private long ticketsSold;
    private BigDecimal grossRevenue;
    private double occupancyRatePercentage;

    public VendorTheaterAnalyticsResponse() {
    }

    public VendorTheaterAnalyticsResponse(UUID theaterId, String theaterName, String cityName,
                                          long screensCount, long showsCount, long ticketsSold,
                                          BigDecimal grossRevenue, double occupancyRatePercentage) {
        this.theaterId = theaterId;
        this.theaterName = theaterName;
        this.cityName = cityName;
        this.screensCount = screensCount;
        this.showsCount = showsCount;
        this.ticketsSold = ticketsSold;
        this.grossRevenue = grossRevenue;
        this.occupancyRatePercentage = occupancyRatePercentage;
    }

    public UUID getTheaterId() {
        return theaterId;
    }

    public void setTheaterId(UUID theaterId) {
        this.theaterId = theaterId;
    }

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public long getScreensCount() {
        return screensCount;
    }

    public void setScreensCount(long screensCount) {
        this.screensCount = screensCount;
    }

    public long getShowsCount() {
        return showsCount;
    }

    public void setShowsCount(long showsCount) {
        this.showsCount = showsCount;
    }

    public long getTicketsSold() {
        return ticketsSold;
    }

    public void setTicketsSold(long ticketsSold) {
        this.ticketsSold = ticketsSold;
    }

    public BigDecimal getGrossRevenue() {
        return grossRevenue;
    }

    public void setGrossRevenue(BigDecimal grossRevenue) {
        this.grossRevenue = grossRevenue;
    }

    public double getOccupancyRatePercentage() {
        return occupancyRatePercentage;
    }

    public void setOccupancyRatePercentage(double occupancyRatePercentage) {
        this.occupancyRatePercentage = occupancyRatePercentage;
    }
}
