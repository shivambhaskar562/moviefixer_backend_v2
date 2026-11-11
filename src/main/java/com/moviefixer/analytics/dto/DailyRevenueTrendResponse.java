package com.moviefixer.analytics.dto;

import java.math.BigDecimal;

public class DailyRevenueTrendResponse {

    private String date;
    private long bookingCount;
    private long ticketsSold;
    private BigDecimal grossRevenue;
    private BigDecimal convenienceFee;

    public DailyRevenueTrendResponse() {
    }

    public DailyRevenueTrendResponse(String date, long bookingCount, long ticketsSold,
                                     BigDecimal grossRevenue, BigDecimal convenienceFee) {
        this.date = date;
        this.bookingCount = bookingCount;
        this.ticketsSold = ticketsSold;
        this.grossRevenue = grossRevenue;
        this.convenienceFee = convenienceFee;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getBookingCount() {
        return bookingCount;
    }

    public void setBookingCount(long bookingCount) {
        this.bookingCount = bookingCount;
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

    public BigDecimal getConvenienceFee() {
        return convenienceFee;
    }

    public void setConvenienceFee(BigDecimal convenienceFee) {
        this.convenienceFee = convenienceFee;
    }
}
