package com.moviefixer.show.dto;

import com.moviefixer.show.enums.ShowStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public class UpdateShowRequest {

    @DecimalMin(value = "1.0", message = "Base price must be at least 1.0")
    @DecimalMax(value = "10000.0", message = "Base price cannot exceed 10000.0")
    private BigDecimal basePrice;

    private ShowStatus status;

    public UpdateShowRequest() {
    }

    public UpdateShowRequest(BigDecimal basePrice, ShowStatus status) {
        this.basePrice = basePrice;
        this.status = status;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public ShowStatus getStatus() {
        return status;
    }

    public void setStatus(ShowStatus status) {
        this.status = status;
    }
}
