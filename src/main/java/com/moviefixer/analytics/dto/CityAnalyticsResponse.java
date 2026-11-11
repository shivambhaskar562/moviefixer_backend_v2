package com.moviefixer.analytics.dto;

import java.math.BigDecimal;

public class CityAnalyticsResponse {

    private Long cityId;
    private String cityName;
    private String stateName;
    private long theatersCount;
    private long screensCount;
    private long showsCount;
    private long ticketsSold;
    private BigDecimal grossRevenue;

    public CityAnalyticsResponse() {
    }

    public CityAnalyticsResponse(Long cityId, String cityName, String stateName,
                                 long theatersCount, long screensCount, long showsCount,
                                 long ticketsSold, BigDecimal grossRevenue) {
        this.cityId = cityId;
        this.cityName = cityName;
        this.stateName = stateName;
        this.theatersCount = theatersCount;
        this.screensCount = screensCount;
        this.showsCount = showsCount;
        this.ticketsSold = ticketsSold;
        this.grossRevenue = grossRevenue;
    }

    public Long getCityId() {
        return cityId;
    }

    public void setCityId(Long cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public long getTheatersCount() {
        return theatersCount;
    }

    public void setTheatersCount(long theatersCount) {
        this.theatersCount = theatersCount;
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
}
