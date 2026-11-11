package com.moviefixer.booking.dto;

import com.moviefixer.booking.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class InitiateBookingResponse {

    private UUID bookingId;
    private String bookingReference;
    private UUID showId;
    private String movieTitle;
    private String theaterName;
    private String screenName;
    private LocalDateTime showStartTime;
    private List<String> seatLabels;
    private BigDecimal totalAmount;
    private BigDecimal convenienceFee;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
    private BookingStatus status;
    private LocalDateTime expiresAt;
    private long expiresInSeconds;

    public InitiateBookingResponse() {
    }

    public InitiateBookingResponse(UUID bookingId, String bookingReference, UUID showId,
                                   String movieTitle, String theaterName, String screenName,
                                   LocalDateTime showStartTime, List<String> seatLabels,
                                   BigDecimal totalAmount, BigDecimal convenienceFee,
                                   BigDecimal discountAmount, BigDecimal netAmount,
                                   BookingStatus status, LocalDateTime expiresAt,
                                   long expiresInSeconds) {
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.showId = showId;
        this.movieTitle = movieTitle;
        this.theaterName = theaterName;
        this.screenName = screenName;
        this.showStartTime = showStartTime;
        this.seatLabels = seatLabels;
        this.totalAmount = totalAmount;
        this.convenienceFee = convenienceFee;
        this.discountAmount = discountAmount;
        this.netAmount = netAmount;
        this.status = status;
        this.expiresAt = expiresAt;
        this.expiresInSeconds = expiresInSeconds;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
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

    public LocalDateTime getShowStartTime() {
        return showStartTime;
    }

    public void setShowStartTime(LocalDateTime showStartTime) {
        this.showStartTime = showStartTime;
    }

    public List<String> getSeatLabels() {
        return seatLabels;
    }

    public void setSeatLabels(List<String> seatLabels) {
        this.seatLabels = seatLabels;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getConvenienceFee() {
        return convenienceFee;
    }

    public void setConvenienceFee(BigDecimal convenienceFee) {
        this.convenienceFee = convenienceFee;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
