package com.moviefixer.theater.dto;

import java.util.List;
import java.util.UUID;

public class ScreenLayoutResponse {

    private UUID screenId;
    private String screenName;
    private UUID theaterId;
    private String theaterName;
    private Integer totalSeats;
    private Integer maxGridRow;
    private Integer maxGridColumn;
    private List<SeatingTierResponse> tiers;
    private List<SeatResponse> seats;

    public ScreenLayoutResponse() {
    }

    public ScreenLayoutResponse(UUID screenId, String screenName, UUID theaterId, String theaterName,
                                Integer totalSeats, Integer maxGridRow, Integer maxGridColumn,
                                List<SeatingTierResponse> tiers, List<SeatResponse> seats) {
        this.screenId = screenId;
        this.screenName = screenName;
        this.theaterId = theaterId;
        this.theaterName = theaterName;
        this.totalSeats = totalSeats;
        this.maxGridRow = maxGridRow;
        this.maxGridColumn = maxGridColumn;
        this.tiers = tiers;
        this.seats = seats;
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

    public Integer getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(Integer totalSeats) {
        this.totalSeats = totalSeats;
    }

    public Integer getMaxGridRow() {
        return maxGridRow;
    }

    public void setMaxGridRow(Integer maxGridRow) {
        this.maxGridRow = maxGridRow;
    }

    public Integer getMaxGridColumn() {
        return maxGridColumn;
    }

    public void setMaxGridColumn(Integer maxGridColumn) {
        this.maxGridColumn = maxGridColumn;
    }

    public List<SeatingTierResponse> getTiers() {
        return tiers;
    }

    public void setTiers(List<SeatingTierResponse> tiers) {
        this.tiers = tiers;
    }

    public List<SeatResponse> getSeats() {
        return seats;
    }

    public void setSeats(List<SeatResponse> seats) {
        this.seats = seats;
    }
}
