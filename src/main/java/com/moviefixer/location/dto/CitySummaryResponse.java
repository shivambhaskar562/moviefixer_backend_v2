package com.moviefixer.location.dto;

import java.util.UUID;

public class CitySummaryResponse {

    private UUID id;
    private String name;
    private String stateName;
    private String countryName;
    private String countryCode;

    public CitySummaryResponse() {
    }

    public CitySummaryResponse(UUID id, String name, String stateName, String countryName, String countryCode) {
        this.id = id;
        this.name = name;
        this.stateName = stateName;
        this.countryName = countryName;
        this.countryCode = countryCode;
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

    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
