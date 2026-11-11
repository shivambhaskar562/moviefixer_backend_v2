package com.moviefixer.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TicketResponse {

    private UUID ticketId;
    private String ticketNumber;
    private String seatRow;
    private Integer seatNumber;
    private String seatLabel;
    private String tierName;
    private BigDecimal price;
    private String qrPayload;
    private boolean checkedIn;
    private LocalDateTime checkedInAt;

    public TicketResponse() {
    }

    public TicketResponse(UUID ticketId, String ticketNumber, String seatRow,
                          Integer seatNumber, String seatLabel, String tierName,
                          BigDecimal price, String qrPayload, boolean checkedIn,
                          LocalDateTime checkedInAt) {
        this.ticketId = ticketId;
        this.ticketNumber = ticketNumber;
        this.seatRow = seatRow;
        this.seatNumber = seatNumber;
        this.seatLabel = seatLabel;
        this.tierName = tierName;
        this.price = price;
        this.qrPayload = qrPayload;
        this.checkedIn = checkedIn;
        this.checkedInAt = checkedInAt;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public void setTicketId(UUID ticketId) {
        this.ticketId = ticketId;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getSeatRow() {
        return seatRow;
    }

    public void setSeatRow(String seatRow) {
        this.seatRow = seatRow;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public String getSeatLabel() {
        return seatLabel;
    }

    public void setSeatLabel(String seatLabel) {
        this.seatLabel = seatLabel;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getQrPayload() {
        return qrPayload;
    }

    public void setQrPayload(String qrPayload) {
        this.qrPayload = qrPayload;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public LocalDateTime getCheckedInAt() {
        return checkedInAt;
    }

    public void setCheckedInAt(LocalDateTime checkedInAt) {
        this.checkedInAt = checkedInAt;
    }
}
