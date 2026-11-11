package com.moviefixer.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class TopMovieAnalyticsResponse {

    private UUID movieId;
    private String movieTitle;
    private String posterUrl;
    private String language;
    private long totalShows;
    private long ticketsSold;
    private BigDecimal grossRevenue;
    private double occupancyRatePercentage;

    public TopMovieAnalyticsResponse() {
    }

    public TopMovieAnalyticsResponse(UUID movieId, String movieTitle, String posterUrl,
                                     String language, long totalShows, long ticketsSold,
                                     BigDecimal grossRevenue, double occupancyRatePercentage) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.posterUrl = posterUrl;
        this.language = language;
        this.totalShows = totalShows;
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

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getTotalShows() {
        return totalShows;
    }

    public void setTotalShows(long totalShows) {
        this.totalShows = totalShows;
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
