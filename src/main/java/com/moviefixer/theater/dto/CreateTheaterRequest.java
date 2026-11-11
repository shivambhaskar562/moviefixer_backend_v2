package com.moviefixer.theater.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public class CreateTheaterRequest {

    @NotBlank(message = "Theater name is required")
    @Size(min = 2, max = 150, message = "Theater name must be between 2 and 150 characters")
    private String name;

    @NotBlank(message = "Theater address is required")
    @Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
    private String address;

    @NotNull(message = "City ID is required")
    private UUID cityId;

    private UUID vendorId; // Optional: If provided by Admin; if null, defaults to authenticated user

    @Pattern(regexp = "^$|^[+0-9\\-\\s()]{7,20}$", message = "Invalid contact phone number format")
    private String contactPhone;

    @Email(message = "Invalid contact email address format")
    private String contactEmail;

    @DecimalMin(value = "-90.0", message = "Latitude must be >= -90.0")
    @DecimalMax(value = "90.0", message = "Latitude must be <= 90.0")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be >= -180.0")
    @DecimalMax(value = "180.0", message = "Longitude must be <= 180.0")
    private BigDecimal longitude;

    public CreateTheaterRequest() {
    }

    public CreateTheaterRequest(String name, String address, UUID cityId, UUID vendorId, String contactPhone, String contactEmail, BigDecimal latitude, BigDecimal longitude) {
        this.name = name;
        this.address = address;
        this.cityId = cityId;
        this.vendorId = vendorId;
        this.contactPhone = contactPhone;
        this.contactEmail = contactEmail;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public UUID getVendorId() {
        return vendorId;
    }

    public void setVendorId(UUID vendorId) {
        this.vendorId = vendorId;
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
}
