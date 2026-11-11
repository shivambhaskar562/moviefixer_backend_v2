package com.moviefixer.booking.dto;

import jakarta.validation.constraints.NotBlank;

public class ValidateQrRequest {

    @NotBlank(message = "QR payload cannot be blank")
    private String qrPayload;

    public ValidateQrRequest() {
    }

    public ValidateQrRequest(String qrPayload) {
        this.qrPayload = qrPayload;
    }

    public String getQrPayload() {
        return qrPayload;
    }

    public void setQrPayload(String qrPayload) {
        this.qrPayload = qrPayload;
    }
}
