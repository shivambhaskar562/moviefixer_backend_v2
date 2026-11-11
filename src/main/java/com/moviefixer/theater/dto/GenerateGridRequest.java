package com.moviefixer.theater.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GenerateGridRequest {

    @NotNull(message = "Total rows is required")
    @Min(value = 1, message = "Total rows must be at least 1")
    @Max(value = 26, message = "Total rows cannot exceed 26 (A-Z)")
    private Integer totalRows;

    @NotNull(message = "Seats per row is required")
    @Min(value = 1, message = "Seats per row must be at least 1")
    @Max(value = 50, message = "Seats per row cannot exceed 50")
    private Integer seatsPerRow;

    @NotNull(message = "Default tier ID is required")
    private UUID defaultTierId;

    // Optional map of row letter to specific tier ID (e.g. {"A": <VIP_UUID>, "B": <VIP_UUID>})
    private Map<String, UUID> rowTierOverrides;

    // Optional column numbers that represent aisles (e.g. [5, 15])
    private List<Integer> aisleColumns;

    // Optional seat identifiers designated for wheelchairs (e.g. ["A1", "A2"])
    private List<String> wheelchairSeats;

    public GenerateGridRequest() {
    }

    public GenerateGridRequest(Integer totalRows, Integer seatsPerRow, UUID defaultTierId,
                               Map<String, UUID> rowTierOverrides, List<Integer> aisleColumns,
                               List<String> wheelchairSeats) {
        this.totalRows = totalRows;
        this.seatsPerRow = seatsPerRow;
        this.defaultTierId = defaultTierId;
        this.rowTierOverrides = rowTierOverrides;
        this.aisleColumns = aisleColumns;
        this.wheelchairSeats = wheelchairSeats;
    }

    public Integer getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Integer totalRows) {
        this.totalRows = totalRows;
    }

    public Integer getSeatsPerRow() {
        return seatsPerRow;
    }

    public void setSeatsPerRow(Integer seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
    }

    public UUID getDefaultTierId() {
        return defaultTierId;
    }

    public void setDefaultTierId(UUID defaultTierId) {
        this.defaultTierId = defaultTierId;
    }

    public Map<String, UUID> getRowTierOverrides() {
        return rowTierOverrides;
    }

    public void setRowTierOverrides(Map<String, UUID> rowTierOverrides) {
        this.rowTierOverrides = rowTierOverrides;
    }

    public List<Integer> getAisleColumns() {
        return aisleColumns;
    }

    public void setAisleColumns(List<Integer> aisleColumns) {
        this.aisleColumns = aisleColumns;
    }

    public List<String> getWheelchairSeats() {
        return wheelchairSeats;
    }

    public void setWheelchairSeats(List<String> wheelchairSeats) {
        this.wheelchairSeats = wheelchairSeats;
    }
}
