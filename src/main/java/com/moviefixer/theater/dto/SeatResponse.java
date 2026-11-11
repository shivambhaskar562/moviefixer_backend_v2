package com.moviefixer.theater.dto;

import com.moviefixer.theater.enums.TierType;

import java.math.BigDecimal;
import java.util.UUID;

public class SeatResponse {

    private UUID id;
    private UUID screenId;
    private String rowIdentifier;
    private Integer seatNumber;
    private UUID tierId;
    private String tierName;
    private TierType tierType;
    private BigDecimal defaultMultiplier;
    private Integer gridRow;
    private Integer gridColumn;
    private boolean isAisle;
    private boolean isWheelchairAccessible;
    private boolean active;

    public SeatResponse() {
    }

    public SeatResponse(UUID id, UUID screenId, String rowIdentifier, Integer seatNumber, UUID tierId,
                        String tierName, TierType tierType, BigDecimal defaultMultiplier, Integer gridRow,
                        Integer gridColumn, boolean isAisle, boolean isWheelchairAccessible, boolean active) {
        this.id = id;
        this.screenId = screenId;
        this.rowIdentifier = rowIdentifier;
        this.seatNumber = seatNumber;
        this.tierId = tierId;
        this.tierName = tierName;
        this.tierType = tierType;
        this.defaultMultiplier = defaultMultiplier;
        this.gridRow = gridRow;
        this.gridColumn = gridColumn;
        this.isAisle = isAisle;
        this.isWheelchairAccessible = isWheelchairAccessible;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getScreenId() {
        return screenId;
    }

    public void setScreenId(UUID screenId) {
        this.screenId = screenId;
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

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public TierType getTierType() {
        return tierType;
    }

    public void setTierType(TierType tierType) {
        this.tierType = tierType;
    }

    public BigDecimal getDefaultMultiplier() {
        return defaultMultiplier;
    }

    public void setDefaultMultiplier(BigDecimal defaultMultiplier) {
        this.defaultMultiplier = defaultMultiplier;
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

    public boolean isAisle() {
        return isAisle;
    }

    public void setAisle(boolean aisle) {
        isAisle = aisle;
    }

    public boolean isWheelchairAccessible() {
        return isWheelchairAccessible;
    }

    public void setWheelchairAccessible(boolean wheelchairAccessible) {
        isWheelchairAccessible = wheelchairAccessible;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
