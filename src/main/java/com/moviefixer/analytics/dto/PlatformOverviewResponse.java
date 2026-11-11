package com.moviefixer.analytics.dto;

import java.math.BigDecimal;

public class PlatformOverviewResponse {

    private BigDecimal totalGrossRevenue;
    private BigDecimal totalConvenienceFee;
    private BigDecimal totalVendorPayout;
    private long totalBookings;
    private long totalConfirmedBookings;
    private long totalCancelledBookings;
    private double cancellationRatePercentage;
    private long totalTicketsSold;
    private long totalTheaters;
    private long totalScreens;
    private long totalShows;
    private BigDecimal averageTicketPrice;

    public PlatformOverviewResponse() {
    }

    public PlatformOverviewResponse(BigDecimal totalGrossRevenue, BigDecimal totalConvenienceFee,
                                  BigDecimal totalVendorPayout, long totalBookings,
                                  long totalConfirmedBookings, long totalCancelledBookings,
                                  double cancellationRatePercentage, long totalTicketsSold,
                                  long totalTheaters, long totalScreens, long totalShows,
                                  BigDecimal averageTicketPrice) {
        this.totalGrossRevenue = totalGrossRevenue;
        this.totalConvenienceFee = totalConvenienceFee;
        this.totalVendorPayout = totalVendorPayout;
        this.totalBookings = totalBookings;
        this.totalConfirmedBookings = totalConfirmedBookings;
        this.totalCancelledBookings = totalCancelledBookings;
        this.cancellationRatePercentage = cancellationRatePercentage;
        this.totalTicketsSold = totalTicketsSold;
        this.totalTheaters = totalTheaters;
        this.totalScreens = totalScreens;
        this.totalShows = totalShows;
        this.averageTicketPrice = averageTicketPrice;
    }

    public BigDecimal getTotalGrossRevenue() {
        return totalGrossRevenue;
    }

    public void setTotalGrossRevenue(BigDecimal totalGrossRevenue) {
        this.totalGrossRevenue = totalGrossRevenue;
    }

    public BigDecimal getTotalConvenienceFee() {
        return totalConvenienceFee;
    }

    public void setTotalConvenienceFee(BigDecimal totalConvenienceFee) {
        this.totalConvenienceFee = totalConvenienceFee;
    }

    public BigDecimal getTotalVendorPayout() {
        return totalVendorPayout;
    }

    public void setTotalVendorPayout(BigDecimal totalVendorPayout) {
        this.totalVendorPayout = totalVendorPayout;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getTotalConfirmedBookings() {
        return totalConfirmedBookings;
    }

    public void setTotalConfirmedBookings(long totalConfirmedBookings) {
        this.totalConfirmedBookings = totalConfirmedBookings;
    }

    public long getTotalCancelledBookings() {
        return totalCancelledBookings;
    }

    public void setTotalCancelledBookings(long totalCancelledBookings) {
        this.totalCancelledBookings = totalCancelledBookings;
    }

    public double getCancellationRatePercentage() {
        return cancellationRatePercentage;
    }

    public void setCancellationRatePercentage(double cancellationRatePercentage) {
        this.cancellationRatePercentage = cancellationRatePercentage;
    }

    public long getTotalTicketsSold() {
        return totalTicketsSold;
    }

    public void setTotalTicketsSold(long totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }

    public long getTotalTheaters() {
        return totalTheaters;
    }

    public void setTotalTheaters(long totalTheaters) {
        this.totalTheaters = totalTheaters;
    }

    public long getTotalScreens() {
        return totalScreens;
    }

    public void setTotalScreens(long totalScreens) {
        this.totalScreens = totalScreens;
    }

    public long getTotalShows() {
        return totalShows;
    }

    public void setTotalShows(long totalShows) {
        this.totalShows = totalShows;
    }

    public BigDecimal getAverageTicketPrice() {
        return averageTicketPrice;
    }

    public void setAverageTicketPrice(BigDecimal averageTicketPrice) {
        this.averageTicketPrice = averageTicketPrice;
    }
}
