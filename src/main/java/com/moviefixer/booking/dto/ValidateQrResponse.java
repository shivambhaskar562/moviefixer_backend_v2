package com.moviefixer.booking.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ValidateQrResponse {

    private boolean valid;
    private boolean admitted;
    private String message;
    private String ticketNumber;
    private String bookingReference;
    private String customerName;
    private String movieTitle;
    private String theaterName;
    private String screenName;
    private String seatLabel;
    private String tierName;
    private LocalDateTime showStartTime;
    private boolean alreadyCheckedIn;
    private LocalDateTime checkedInAt;
    private UUID checkedInByUserId;

    public ValidateQrResponse() {
    }

    public ValidateQrResponse(boolean valid, boolean admitted, String message,
                              String ticketNumber, String bookingReference, String customerName,
                              String movieTitle, String theaterName, String screenName,
                              String seatLabel, String tierName, LocalDateTime showStartTime,
                              boolean alreadyCheckedIn, LocalDateTime checkedInAt,
                              UUID checkedInByUserId) {
        this.valid = valid;
        this.admitted = admitted;
        this.message = message;
        this.ticketNumber = ticketNumber;
        this.bookingReference = bookingReference;
        this.customerName = customerName;
        this.movieTitle = movieTitle;
        this.theaterName = theaterName;
        this.screenName = screenName;
        this.seatLabel = seatLabel;
        this.tierName = tierName;
        this.showStartTime = showStartTime;
        this.alreadyCheckedIn = alreadyCheckedIn;
        this.checkedInAt = checkedInAt;
        this.checkedInByUserId = checkedInByUserId;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isAdmitted() {
        return admitted;
    }

    public void setAdmitted(boolean admitted) {
        this.admitted = admitted;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
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

    public String getSeatLabel() {
        return seatLabel;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public LocalDateTime getShowStartTime() {
        return showStartTime;
    }

    public void setShowStartTime(LocalDateTime showStartTime) {
        this.showStartTime = showStartTime;
    }

    public boolean isAlreadyCheckedIn() {
        return alreadyCheckedIn;
    }

    public void setAlreadyCheckedIn(boolean alreadyCheckedIn) {
        this.alreadyCheckedIn = alreadyCheckedIn;
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(LocalDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }

    public UUID getCheckedInByUserId() {
        return checkedInByUserId;
    }

    public void setCheckedInByUserId(UUID checkedInByUserId) {
        this.checkedInByUserId = checkedInByUserId;
    }
}
