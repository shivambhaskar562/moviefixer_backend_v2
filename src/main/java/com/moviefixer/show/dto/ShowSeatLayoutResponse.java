package com.moviefixer.show.dto;

import com.moviefixer.theater.enums.ScreenType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ShowSeatLayoutResponse {

    private UUID showId;
    private UUID movieId;
    private String movieTitle;
    private UUID theaterId;
    private String theaterName;
    private UUID screenId;
    private String screenName;
    private ScreenType screenType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal basePrice;
    private long totalSeats;
    private long availableSeats;
    private List<ShowSeatResponse> seats;

    public ShowSeatLayoutResponse() {
    }

    public ShowSeatLayoutResponse(UUID showId, UUID movieId, String movieTitle, UUID theaterId, String theaterName,
                                  UUID screenId, String screenName, ScreenType screenType, LocalDateTime startTime,
                                  LocalDateTime endTime, BigDecimal basePrice, long totalSeats, long availableSeats,
                                  List<ShowSeatResponse> seats) {
        this.showId = showId;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.theaterId = theaterId;
        this.theaterName = theaterName;
        this.screenId = screenId;
        this.screenName = screenName;
        this.screenType = screenType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.basePrice = basePrice;
        this.totalSeats = totalSeats;
        this.availableSeats = availableSeats;
        this.seats = seats;
    }

    public UUID getShowId() {
        return showId;
    }

    public void setShowId(UUID showId) {
        this.showId = showId;
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

    public UUID getScreenId() {
        return screenId;
    }

    public void setScreenId(UUID screenId) {
        this.screenId = screenId;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public long getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(long totalSeats) {
        this.totalSeats = totalSeats;
    }

    public long getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(long availableSeats) {
        this.availableSeats = availableSeats;
    }

    public List<ShowSeatResponse> getSeats() {
        return seats;
    }

    public void setSeats(List<ShowSeatResponse> seats) {
        this.seats = seats;
    }
}
