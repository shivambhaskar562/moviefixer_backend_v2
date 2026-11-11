package com.moviefixer.booking.dto;

import com.moviefixer.booking.enums.BookingStatus;
import com.moviefixer.theater.enums.ScreenType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BookingResponse {

    private UUID bookingId;
    private String bookingReference;
    private UUID showId;
    private String movieTitle;
    private String posterUrl;
    private String theaterName;
    private String theaterAddress;
    private String screenName;
    private ScreenType screenType;
    private LocalDateTime showStartTime;
    private LocalDateTime showEndTime;
    private BigDecimal totalAmount;
    private BigDecimal convenienceFee;
    private BigDecimal discountAmount;
    private BigDecimal netAmount;
    private BookingStatus status;
    private LocalDateTime expiresAt;
    private Instant createdAt;
    private List<TicketResponse> tickets;

    public BookingResponse() {
    }

    public BookingResponse(UUID bookingId, String bookingReference, UUID showId,
                           String movieTitle, String posterUrl, String theaterName,
                           String theaterAddress, String screenName, ScreenType screenType,
                           LocalDateTime showStartTime, LocalDateTime showEndTime,
                           BigDecimal totalAmount, BigDecimal convenienceFee,
                           BigDecimal discountAmount, BigDecimal netAmount,
                           BookingStatus status, LocalDateTime expiresAt,
                           Instant createdAt, List<TicketResponse> tickets) {
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.showId = showId;
        this.movieTitle = movieTitle;
        this.posterUrl = posterUrl;
        this.theaterName = theaterName;
        this.theaterAddress = theaterAddress;
        this.screenName = screenName;
        this.screenType = screenType;
        this.showStartTime = showStartTime;
        this.showEndTime = showEndTime;
        this.totalAmount = totalAmount;
        this.convenienceFee = convenienceFee;
        this.discountAmount = discountAmount;
        this.netAmount = netAmount;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.tickets = tickets;
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

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public String getTheaterAddress() {
        return theaterAddress;
    }

    public void setTheaterAddress(String theaterAddress) {
        this.theaterAddress = theaterAddress;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public ScreenType getScreenType() {
        return screenType;
    }

    public void setScreenType(ScreenType screenType) {
        this.screenType = screenType;
    }

    public LocalDateTime getShowStartTime() {
        return showStartTime;
    }

    public void setShowStartTime(LocalDateTime showStartTime) {
        this.showStartTime = showStartTime;
    }

    public LocalDateTime getShowEndTime() {
        return showEndTime;
    }

    public void setShowEndTime(LocalDateTime showEndTime) {
        this.showEndTime = showEndTime;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<TicketResponse> getTickets() {
        return tickets;
    }

    public void setTickets(List<TicketResponse> tickets) {
        this.tickets = tickets;
    }
}
