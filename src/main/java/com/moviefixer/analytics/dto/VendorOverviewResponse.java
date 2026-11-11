package com.moviefixer.analytics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public class VendorOverviewResponse {

    private UUID vendorId;
    private String vendorName;
    private long totalTheaters;
    private long totalScreens;
    private long totalShows;
    private long totalTicketsSold;
    private BigDecimal grossBoxOfficeRevenue;
    private BigDecimal platformFeeDeducted;
    private BigDecimal netVendorEarnings;
    private long admissionsCount;
    private double gateCheckInRatePercentage;
    private double overallOccupancyRatePercentage;

    public VendorOverviewResponse() {
    }

    public VendorOverviewResponse(UUID vendorId, String vendorName, long totalTheaters,
                                  long totalScreens, long totalShows, long totalTicketsSold,
                                  BigDecimal grossBoxOfficeRevenue, BigDecimal platformFeeDeducted,
                                  BigDecimal netVendorEarnings, long admissionsCount,
                                  double gateCheckInRatePercentage, double overallOccupancyRatePercentage) {
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.totalTheaters = totalTheaters;
        this.totalScreens = totalScreens;
        this.totalShows = totalShows;
        this.totalTicketsSold = totalTicketsSold;
        this.grossBoxOfficeRevenue = grossBoxOfficeRevenue;
        this.platformFeeDeducted = platformFeeDeducted;
        this.netVendorEarnings = netVendorEarnings;
        this.admissionsCount = admissionsCount;
        this.gateCheckInRatePercentage = gateCheckInRatePercentage;
        this.overallOccupancyRatePercentage = overallOccupancyRatePercentage;
    }

    public UUID getVendorId() {
        return vendorId;
    }

    public void setVendorId(UUID vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
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

    public long getTotalTicketsSold() {
        return totalTicketsSold;
    }

    public void setTotalTicketsSold(long totalTicketsSold) {
        this.totalTicketsSold = totalTicketsSold;
    }

    public BigDecimal getGrossBoxOfficeRevenue() {
        return grossBoxOfficeRevenue;
    }

    public void setGrossBoxOfficeRevenue(BigDecimal grossBoxOfficeRevenue) {
        this.grossBoxOfficeRevenue = grossBoxOfficeRevenue;
    }

    public BigDecimal getPlatformFeeDeducted() {
        return platformFeeDeducted;
    }

    public void setPlatformFeeDeducted(BigDecimal platformFeeDeducted) {
        this.platformFeeDeducted = platformFeeDeducted;
    }

    public BigDecimal getNetVendorEarnings() {
        return netVendorEarnings;
    }

    public void setNetVendorEarnings(BigDecimal netVendorEarnings) {
        this.netVendorEarnings = netVendorEarnings;
    }

    public long getAdmissionsCount() {
        return admissionsCount;
    }

    public void setAdmissionsCount(long admissionsCount) {
        this.admissionsCount = admissionsCount;
    }

    public double getGateCheckInRatePercentage() {
        return gateCheckInRatePercentage;
    }

    public void setGateCheckInRatePercentage(double gateCheckInRatePercentage) {
        this.gateCheckInRatePercentage = gateCheckInRatePercentage;
    }

    public double getOverallOccupancyRatePercentage() {
        return overallOccupancyRatePercentage;
    }

    public void setOverallOccupancyRatePercentage(double overallOccupancyRatePercentage) {
        this.overallOccupancyRatePercentage = overallOccupancyRatePercentage;
    }
}
