package com.moviefixer.show.dto;

import com.moviefixer.show.enums.ShowSeatStatus;
import com.moviefixer.theater.enums.TierType;

import java.math.BigDecimal;
import java.util.UUID;

public class ShowSeatResponse {

    private UUID showSeatId;
    private UUID seatId;
    private String rowIdentifier;
    private Integer seatNumber;
    private UUID tierId;
    private String tierName;
    private TierType tierType;
    private BigDecimal price;
    private ShowSeatStatus status;
    private Integer gridRow;
    private Integer gridColumn;
    private boolean isAisle;
    private boolean isWheelchairAccessible;
    private boolean isLockedByCurrentUser;
    private Long version;

    public ShowSeatResponse() {
    }

    public ShowSeatResponse(UUID showSeatId, UUID seatId, String rowIdentifier, Integer seatNumber,
                            UUID tierId, String tierName, TierType tierType, BigDecimal price,
                            ShowSeatStatus status, Integer gridRow, Integer gridColumn,
                            boolean isAisle, boolean isWheelchairAccessible, boolean isLockedByCurrentUser,
                            Long version) {
        this.showSeatId = showSeatId;
        this.seatId = seatId;
        this.rowIdentifier = rowIdentifier;
        this.seatNumber = seatNumber;
        this.tierId = tierId;
        this.tierName = tierName;
        this.tierType = tierType;
        this.price = price;
        this.status = status;
        this.gridRow = gridRow;
        this.gridColumn = gridColumn;
        this.isAisle = isAisle;
        this.isWheelchairAccessible = isWheelchairAccessible;
        this.isLockedByCurrentUser = isLockedByCurrentUser;
        this.version = version;
    }

    public UUID getShowSeatId() {
        return showSeatId;
    }

    public void setShowSeatId(UUID showSeatId) {
        this.showSeatId = showSeatId;
    }

    public UUID getSeatId() {
        return seatId;
    }

    public void setSeatId(UUID seatId) {
        this.seatId = seatId;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public ShowSeatStatus getStatus() {
        return status;
    }

    public void setStatus(ShowSeatStatus status) {
        this.status = status;
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

    public boolean isLockedByCurrentUser() {
        return isLockedByCurrentUser;
    }

    public void setLockedByCurrentUser(boolean lockedByCurrentUser) {
        this.isLockedByCurrentUser = lockedByCurrentUser;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
