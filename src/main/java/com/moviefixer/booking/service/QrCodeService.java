package com.moviefixer.booking.service;

import com.moviefixer.booking.entity.Ticket;

public interface QrCodeService {

    /**
     * Generates a tamper-proof cryptographically signed digital QR payload for a ticket.
     *
     * @param ticket the ticket entity with associated booking and seat information
     * @return cryptographically signed Base64 URL-safe payload string
     */
    String generateSignedQrPayload(Ticket ticket);

    /**
     * Verifies the authenticity and integrity of a signed QR payload.
     *
     * @param qrPayload the QR payload scanned by gate staff
     * @return the verified ticket number extracted from the payload
     * @throws IllegalArgumentException if payload structure is invalid
     * @throws SecurityException if signature verification fails (tampering/forgery)
     */
    String verifyAndExtractTicketNumber(String qrPayload);
}
