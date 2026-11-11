package com.moviefixer.booking.dto;

import com.moviefixer.booking.enums.BookingStatus;

import java.math.BigDecimal;
import java.util.UUID;

public class CancelBookingResponse {

    private UUID bookingId;
    private String bookingReference;
    private BookingStatus status;
    private BigDecimal originalTotal;
    private BigDecimal refundAmount;
    private BigDecimal cancellationPenalty;
    private String message;

    public CancelBookingResponse() {
    }

    public CancelBookingResponse(UUID bookingId, String bookingReference, BookingStatus status,
                                 BigDecimal originalTotal, BigDecimal refundAmount,
                                 BigDecimal cancellationPenalty, String message) {
        this.bookingId = bookingId;
        this.bookingReference = bookingReference;
        this.status = status;
        this.originalTotal = originalTotal;
        this.refundAmount = refundAmount;
        this.cancellationPenalty = cancellationPenalty;
        this.message = message;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public void setBookingId(UUID bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public BigDecimal getOriginalTotal() {
        return originalTotal;
    }

    public void setOriginalTotal(BigDecimal originalTotal) {
        this.originalTotal = originalTotal;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getCancellationPenalty() {
        return cancellationPenalty;
    }

    public void setCancellationPenalty(BigDecimal cancellationPenalty) {
        this.cancellationPenalty = cancellationPenalty;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
