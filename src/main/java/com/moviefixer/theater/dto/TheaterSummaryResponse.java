package com.moviefixer.theater.dto;

import java.util.UUID;

public class TheaterSummaryResponse {

    private UUID id;
    private String name;
    private String address;
    private String cityName;
    private String contactPhone;
    private long totalScreens;
    private boolean active;

    public TheaterSummaryResponse() {
    }

    public TheaterSummaryResponse(UUID id, String name, String address, String cityName, String contactPhone, long totalScreens, boolean active) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.cityName = cityName;
        this.contactPhone = contactPhone;
        this.totalScreens = totalScreens;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public long getTotalScreens() {
        return totalScreens;
    }

    public void setTotalScreens(long totalScreens) {
        this.totalScreens = totalScreens;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
