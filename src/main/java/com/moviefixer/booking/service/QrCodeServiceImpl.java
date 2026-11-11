package com.moviefixer.booking.service;

import com.moviefixer.booking.entity.Booking;
import com.moviefixer.booking.entity.Ticket;
import com.moviefixer.show.entity.ShowSeat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;

@Service
public class QrCodeServiceImpl implements QrCodeService {

    private static final Logger log = LoggerFactory.getLogger(QrCodeServiceImpl.class);
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final String qrSecret;

    public QrCodeServiceImpl(@Value("${app.qr.secret}") String qrSecret) {
        this.qrSecret = qrSecret;
    }

    @Override
    public String generateSignedQrPayload(Ticket ticket) {
        if (ticket == null || ticket.getTicketNumber() == null) {
            throw new IllegalArgumentException("Ticket and ticket number cannot be null");
        }

        Booking booking = ticket.getBooking();
        ShowSeat showSeat = ticket.getShowSeat();

        String bookingRef = (booking != null) ? booking.getBookingReference() : "UNKNOWN";
        String showId = (booking != null && booking.getShow() != null) ? booking.getShow().getId().toString() : "UNKNOWN";
        String seatId = (showSeat != null && showSeat.getSeat() != null) ? showSeat.getSeat().getId().toString() : "UNKNOWN";
        long timestamp = Instant.now().toEpochMilli();

        // Plaintext payload format: MF|ticketNumber|bookingRef|showId|seatId|timestamp
        String data = String.format("MF|%s|%s|%s|%s|%d", ticket.getTicketNumber(), bookingRef, showId, seatId, timestamp);
        String signature = computeHmacSha256(data);

        String encodedData = Base64.getUrlEncoder().withoutPadding().encodeToString(data.getBytes(StandardCharsets.UTF_8));
        return encodedData + "." + signature;
    }

    @Override
    public String verifyAndExtractTicketNumber(String qrPayload) {
        if (qrPayload == null || qrPayload.isBlank()) {
            throw new IllegalArgumentException("QR payload cannot be empty");
        }

        String[] parts = qrPayload.split("\\.", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Malformed QR payload structure");
        }

        String encodedData = parts[0];
        String presentedSignature = parts[1];

        String rawData;
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(encodedData);
            rawData = new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 encoding in QR payload", e);
        }

        String expectedSignature = computeHmacSha256(rawData);

        // Constant-time comparison to prevent timing attacks
        byte[] expectedBytes = expectedSignature.getBytes(StandardCharsets.UTF_8);
        byte[] presentedBytes = presentedSignature.getBytes(StandardCharsets.UTF_8);

        if (!MessageDigest.isEqual(expectedBytes, presentedBytes)) {
            log.warn("Cryptographic verification failed for QR payload. Tampered or forged ticket scan attempt.");
            throw new SecurityException("Cryptographic verification failed: QR pass is invalid or has been tampered with");
        }

        // Data format: MF|ticketNumber|bookingRef|showId|seatId|timestamp
        String[] dataFields = rawData.split("\\|");
        if (dataFields.length < 2 || !"MF".equals(dataFields[0])) {
            throw new IllegalArgumentException("Invalid digital ticket pass header or format");
        }

        return dataFields[1];
    }

    private String computeHmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec secretKey = new SecretKeySpec(qrSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (Exception e) {
            log.error("Failed to compute HMAC-SHA256 signature for ticket QR", e);
            throw new IllegalStateException("Cryptographic algorithm unavailable", e);
        }
    }
}
