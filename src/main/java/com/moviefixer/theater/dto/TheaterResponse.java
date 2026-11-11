package com.moviefixer.theater.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TheaterResponse {

    private UUID id;
    private String name;
    private String address;
    private UUID cityId;
    private String cityName;
    private String stateName;
    private String countryName;
    private UUID vendorId;
    private String vendorName;
    private String vendorEmail;
    private String contactPhone;
    private String contactEmail;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private boolean active;
    private long totalScreens;
    private Instant createdAt;
    private Instant updatedAt;

    public TheaterResponse() {
    }

    public TheaterResponse(UUID id, String name, String address, UUID cityId, String cityName, String stateName,
                           String countryName, UUID vendorId, String vendorName, String vendorEmail,
                           String contactPhone, String contactEmail, BigDecimal latitude, BigDecimal longitude,
                           boolean active, long totalScreens, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.cityId = cityId;
        this.cityName = cityName;
        this.stateName = stateName;
        this.countryName = countryName;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.vendorEmail = vendorEmail;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.latitude = latitude;
        this.longitude = longitude;
        this.active = active;
        this.totalScreens = totalScreens;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public UUID getCityId() {
        return cityId;
    }

    public void setCityId(UUID cityId) {
        this.cityId = cityId;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
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

    public UUID getVendorId() {
        return vendorId;
    }

    public void setVendorId(UUID vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVendorEmail() {
        return vendorEmail;
    }

    public void setVendorEmail(String vendorEmail) {
        this.vendorEmail = vendorEmail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getTotalScreens() {
        return totalScreens;
    }

    public void setTotalScreens(long totalScreens) {
        this.totalScreens = totalScreens;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
