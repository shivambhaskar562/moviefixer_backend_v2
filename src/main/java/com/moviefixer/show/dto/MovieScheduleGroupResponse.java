package com.moviefixer.show.dto;

import java.util.List;
import java.util.UUID;

public class MovieScheduleGroupResponse {

    private UUID theaterId;
    private String theaterName;
    private String theaterAddress;
    private String cityName;
    private List<ScreenScheduleDto> screens;

    public MovieScheduleGroupResponse() {
    }

    public MovieScheduleGroupResponse(UUID theaterId, String theaterName, String theaterAddress, String cityName, List<ScreenScheduleDto> screens) {
        this.theaterId = theaterId;
        this.theaterName = theaterName;
        this.theaterAddress = theaterAddress;
        this.cityName = cityName;
        this.screens = screens;
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

    public String getTheaterAddress() {
        return theaterAddress;
    }

    public void setTheaterAddress(String theaterAddress) {
        this.theaterAddress = theaterAddress;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public List<ScreenScheduleDto> getScreens() {
        return screens;
    }

    public void setScreens(List<ScreenScheduleDto> screens) {
        this.screens = screens;
    }

    public static class ScreenScheduleDto {
        private UUID screenId;
        private String screenName;
        private String screenType;
        private String soundSystem;
        private List<ShowSummaryResponse> showtimes;

        public ScreenScheduleDto() {
        }

        public ScreenScheduleDto(UUID screenId, String screenName, String screenType, String soundSystem, List<ShowSummaryResponse> showtimes) {
            this.screenId = screenId;
            this.screenName = screenName;
            this.screenType = screenType;
            this.soundSystem = soundSystem;
            this.showtimes = showtimes;
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

        public String getScreenType() {
            return screenType;
        }

        public void setScreenType(String screenType) {
            this.screenType = screenType;
        }

        public String getSoundSystem() {
            return soundSystem;
        }

        public void setSoundSystem(String soundSystem) {
            this.soundSystem = soundSystem;
        }

        public List<ShowSummaryResponse> getShowtimes() {
            return showtimes;
        }

        public void setShowtimes(List<ShowSummaryResponse> showtimes) {
            this.showtimes = showtimes;
        }
    }
}
