package com.moviefixer.booking.service;

import com.moviefixer.booking.entity.Booking;
import com.moviefixer.booking.entity.Ticket;
import com.moviefixer.show.entity.Show;
import com.moviefixer.show.entity.ShowSeat;
import com.moviefixer.theater.entity.Seat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QrCodeService Unit Tests (Cryptographic HMAC-SHA256 Signed Passes)")
class QrCodeServiceTest {

    private QrCodeServiceImpl qrCodeService;
    private static final String TEST_SECRET = "test_qr_hmac_secret_key_moviefixer_2026_xyz!@#";

    @BeforeEach
    void setUp() {
        qrCodeService = new QrCodeServiceImpl(TEST_SECRET);
    }

    @Test
    @DisplayName("Should successfully generate signed QR payload and verify ticket number")
    void shouldGenerateAndVerifyQrPayload() {
        Booking booking = new Booking();
        booking.setId(UUID.randomUUID());
        booking.setBookingReference("MF-20261010-ABC123");

        Show show = new Show();
        show.setId(UUID.randomUUID());
        booking.setShow(show);

        Seat seat = new Seat();
        seat.setId(UUID.randomUUID());
        seat.setRowIdentifier("C");
        seat.setSeatNumber(5);

        ShowSeat showSeat = new ShowSeat();
        showSeat.setId(UUID.randomUUID());
        showSeat.setSeat(seat);

        Ticket ticket = new Ticket();
        ticket.setId(UUID.randomUUID());
        ticket.setTicketNumber("TKT-20261010-ABC123-01");
        ticket.setBooking(booking);
        ticket.setShowSeat(showSeat);

        // Generate signed payload
        String qrPayload = qrCodeService.generateSignedQrPayload(ticket);
        assertNotNull(qrPayload);
        assertTrue(qrPayload.contains("."));

        // Verify and extract ticket number
        String extractedTicketNumber = qrCodeService.verifyAndExtractTicketNumber(qrPayload);
        assertEquals("TKT-20261010-ABC123-01", extractedTicketNumber);
    }

    @Test
    @DisplayName("Should reject forged QR payload with SecurityException")
    void shouldRejectForgedQrPayload() {
        Booking booking = new Booking();
        booking.setBookingReference("MF-20261010-ABC123");

        Ticket ticket = new Ticket();
        ticket.setTicketNumber("TKT-20261010-ABC123-01");
        ticket.setBooking(booking);

        String validPayload = qrCodeService.generateSignedQrPayload(ticket);
        String[] parts = validPayload.split("\\.");

        // Tamper with signature
        String forgedPayload = parts[0] + ".forgedSignatureFake123";

        assertThrows(SecurityException.class, () -> qrCodeService.verifyAndExtractTicketNumber(forgedPayload));
    }

    @Test
    @DisplayName("Should reject malformed QR payload with IllegalArgumentException")
    void shouldRejectMalformedQrPayload() {
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.verifyAndExtractTicketNumber(null));
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.verifyAndExtractTicketNumber(""));
        assertThrows(IllegalArgumentException.class, () -> qrCodeService.verifyAndExtractTicketNumber("not-a-valid-payload-no-dots"));
    }
}
