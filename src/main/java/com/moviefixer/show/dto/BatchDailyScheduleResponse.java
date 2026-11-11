package com.moviefixer.show.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class BatchDailyScheduleResponse {

    private UUID movieId;
    private String movieTitle;
    private UUID screenId;
    private String screenName;
    private String theaterName;
    private LocalDate date;
    private int scheduledShowsCount;
    private List<ShowResponse> shows;

    public BatchDailyScheduleResponse() {
    }

    public BatchDailyScheduleResponse(UUID movieId, String movieTitle, UUID screenId, String screenName,
                                      String theaterName, LocalDate date, int scheduledShowsCount,
                                      List<ShowResponse> shows) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.screenId = screenId;
        this.screenName = screenName;
        this.theaterName = theaterName;
        this.date = date;
        this.scheduledShowsCount = scheduledShowsCount;
        this.shows = shows;
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

    public String getTheaterName() {
        return theaterName;
    }

    public void setTheaterName(String theaterName) {
        this.theaterName = theaterName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getScheduledShowsCount() {
        return scheduledShowsCount;
    }

    public void setScheduledShowsCount(int scheduledShowsCount) {
        this.scheduledShowsCount = scheduledShowsCount;
    }

    public List<ShowResponse> getShows() {
        return shows;
    }

    public void setShows(List<ShowResponse> shows) {
        this.shows = shows;
    }
}
