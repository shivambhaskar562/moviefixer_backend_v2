package com.moviefixer.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class VendorMovieAnalyticsResponse {

    private UUID movieId;
    private String movieTitle;
    private String language;
    private long showsCount;
    private long ticketsSold;
    private BigDecimal grossRevenue;
    private double occupancyRatePercentage;

    public VendorMovieAnalyticsResponse() {
    }

    public VendorMovieAnalyticsResponse(UUID movieId, String movieTitle, String language,
                                        long showsCount, long ticketsSold,
                                        BigDecimal grossRevenue, double occupancyRatePercentage) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.language = language;
        this.showsCount = showsCount;
        this.ticketsSold = ticketsSold;
        this.grossRevenue = grossRevenue;
        this.occupancyRatePercentage = occupancyRatePercentage;
    }

    public UUID getMovieId() {
        return movieId;
    }

    public void setMovieId(UUID movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
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
