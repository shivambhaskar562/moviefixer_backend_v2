package com.moviefixer.theater.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class SeatItemDto {

    @NotBlank(message = "Row identifier is required (e.g. A, B, C)")
    @Size(min = 1, max = 10, message = "Row identifier must be between 1 and 10 characters")
    private String rowIdentifier;

    @NotNull(message = "Seat number is required")
    @Min(value = 1, message = "Seat number must be >= 1")
    private Integer seatNumber;

    @NotNull(message = "Tier ID is required")
    private UUID tierId;

    @NotNull(message = "Grid row coordinate is required")
    @Min(value = 0, message = "Grid row must be >= 0")
    private Integer gridRow;

    @NotNull(message = "Grid column coordinate is required")
    @Min(value = 0, message = "Grid column must be >= 0")
    private Integer gridColumn;

    private Boolean isAisle = false;

    private Boolean isWheelchairAccessible = false;

    private Boolean active = true;

    public SeatItemDto() {
    }

    public SeatItemDto(String rowIdentifier, Integer seatNumber, UUID tierId, Integer gridRow, Integer gridColumn,
                       Boolean isAisle, Boolean isWheelchairAccessible, Boolean active) {
        this.rowIdentifier = rowIdentifier;
        this.seatNumber = seatNumber;
        this.tierId = tierId;
        this.gridRow = gridRow;
        this.gridColumn = gridColumn;
        this.isAisle = (isAisle != null) ? isAisle : false;
        this.isWheelchairAccessible = (isWheelchairAccessible != null) ? isWheelchairAccessible : false;
        this.active = (active != null) ? active : true;
    }

    public String getRowIdentifier() {
        return rowIdentifier;
    }

    public void setRowIdentifier(String rowIdentifier) {
        this.rowIdentifier = rowIdentifier;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public UUID getTierId() {
        return tierId;
    }

    public void setTierId(UUID tierId) {
        this.tierId = tierId;
    }

    public Integer getGridRow() {
        return gridRow;
    }

    public void setGridRow(Integer gridRow) {
        this.gridRow = gridRow;
    }

    public Integer getGridColumn() {
        return gridColumn;
    }

    public void setGridColumn(Integer gridColumn) {
        this.gridColumn = gridColumn;
    }

    public Boolean getIsAisle() {
        return isAisle;
    }

    public void setIsAisle(Boolean aisle) {
        isAisle = aisle;
    }

    public Boolean getIsWheelchairAccessible() {
        return isWheelchairAccessible;
    }

    public void setIsWheelchairAccessible(Boolean wheelchairAccessible) {
        isWheelchairAccessible = wheelchairAccessible;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
