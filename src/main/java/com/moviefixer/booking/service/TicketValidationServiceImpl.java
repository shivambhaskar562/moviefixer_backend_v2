package com.moviefixer.booking.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.booking.dto.TicketResponse;
import com.moviefixer.booking.dto.ValidateQrRequest;
import com.moviefixer.booking.dto.ValidateQrResponse;
import com.moviefixer.booking.entity.Booking;
import com.moviefixer.booking.entity.Ticket;
import com.moviefixer.booking.enums.BookingStatus;
import com.moviefixer.booking.repository.TicketRepository;
import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.show.entity.Show;
import com.moviefixer.show.entity.ShowSeat;
import com.moviefixer.theater.entity.Screen;
import com.moviefixer.theater.entity.Seat;
import com.moviefixer.theater.entity.Theater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TicketValidationServiceImpl implements TicketValidationService {

    private static final Logger log = LoggerFactory.getLogger(TicketValidationServiceImpl.class);

    private final TicketRepository ticketRepository;
    private final QrCodeService qrCodeService;

    public TicketValidationServiceImpl(TicketRepository ticketRepository, QrCodeService qrCodeService) {
        this.ticketRepository = ticketRepository;
        this.qrCodeService = qrCodeService;
    }

    @Override
    @Transactional
    public ValidateQrResponse validateAndAdmit(ValidateQrRequest request, UserPrincipal gateStaff) {
        if (request == null || request.getQrPayload() == null || request.getQrPayload().isBlank()) {
            throw new IllegalArgumentException("QR payload cannot be empty");
        }

        // Cryptographically verify HMAC-SHA256 signature and extract ticket number
        String ticketNumber = qrCodeService.verifyAndExtractTicketNumber(request.getQrPayload());

        Ticket ticket = ticketRepository.findByTicketNumberWithDetails(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketNumber", ticketNumber));

        Booking booking = ticket.getBooking();
        Show show = (booking != null) ? booking.getShow() : null;
        Screen screen = (show != null) ? show.getScreen() : null;
        Theater theater = (screen != null) ? screen.getTheater() : null;
        ShowSeat showSeat = ticket.getShowSeat();
        Seat seat = (showSeat != null) ? showSeat.getSeat() : null;

        String movieTitle = (show != null && show.getMovie() != null) ? show.getMovie().getTitle() : "Unknown Movie";
        String theaterName = (theater != null) ? theater.getName() : "Unknown Theater";
        String screenName = (screen != null) ? screen.getName() : "Unknown Screen";
        String seatLabel = (seat != null) ? (seat.getRowIdentifier() + seat.getSeatNumber()) : "Unknown Seat";
        String tierName = (seat != null && seat.getTier() != null) ? seat.getTier().getName() : "STANDARD";
        String customerName = (booking != null && booking.getUser() != null) ? booking.getUser().getName() : "Customer";
        String bookingRef = (booking != null) ? booking.getBookingReference() : "Unknown";
        LocalDateTime showStartTime = (show != null) ? show.getStartTime() : null;

        // Check if booking is in CONFIRMED status
        if (booking == null || booking.getStatus() != BookingStatus.CONFIRMED) {
            String status = (booking != null) ? booking.getStatus().name() : "NULL";
            log.warn("Admission Denied: Ticket {} belongs to a booking with status {}", ticketNumber, status);
            return new ValidateQrResponse(
                    false,
                    false,
                    "Admission Denied: Booking is not confirmed (Status: " + status + ")",
                    ticketNumber,
                    bookingRef,
                    customerName,
                    movieTitle,
                    theaterName,
                    screenName,
                    seatLabel,
                    tierName,
                    showStartTime,
                    false,
                    null,
                    null
            );
        }

        // Duplicate admission check: Prevent ticket reuse / sharing
        if (ticket.isCheckedIn()) {
            log.warn("Duplicate admission alert! Ticket {} was already scanned at {} by staff ID {}",
                    ticketNumber, ticket.getCheckedInAt(), ticket.getCheckedInByUserId());
            return new ValidateQrResponse(
                    true,
                    false,
                    String.format("ALREADY ADMITTED: This pass was already scanned at %s. Duplicate entry strictly prohibited.",
                            ticket.getCheckedInAt()),
                    ticketNumber,
                    bookingRef,
                    customerName,
                    movieTitle,
                    theaterName,
                    screenName,
                    seatLabel,
                    tierName,
                    showStartTime,
                    true,
                    ticket.getCheckedInAt(),
                    ticket.getCheckedInByUserId()
            );
        }

        // First-time valid check-in: Admit customer
        LocalDateTime scanTime = LocalDateTime.now();
        ticket.setCheckedIn(true);
        ticket.setCheckedInAt(scanTime);
        if (gateStaff != null) {
            ticket.setCheckedInByUserId(gateStaff.getId());
        }
        ticketRepository.save(ticket);

        log.info("Ticket admitted successfully: ticketNumber={}, seat={}, customer={}",
                ticketNumber, seatLabel, customerName);

        return new ValidateQrResponse(
                true,
                true,
                "Admission Approved: Valid ticket pass. Welcome to the show!",
                ticketNumber,
                bookingRef,
                customerName,
                movieTitle,
                theaterName,
                screenName,
                seatLabel,
                tierName,
                showStartTime,
                false,
                scanTime,
                (gateStaff != null) ? gateStaff.getId() : null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketByNumber(String ticketNumber, UserPrincipal currentUser) {
        Ticket ticket = ticketRepository.findByTicketNumberWithDetails(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "ticketNumber", ticketNumber));

        ShowSeat ss = ticket.getShowSeat();
        Seat st = (ss != null) ? ss.getSeat() : null;
        String tierName = (st != null && st.getTier() != null) ? st.getTier().getName() : "STANDARD";
        String row = (st != null) ? st.getRowIdentifier() : "";
        Integer num = (st != null) ? st.getSeatNumber() : 0;
        BigDecimal price = (ss != null) ? ss.getPrice() : BigDecimal.ZERO;

        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                row,
                num,
                row + num,
                tierName,
                price,
                ticket.getQrPayload(),
                ticket.isCheckedIn(),
                ticket.getCheckedInAt()
        );
    }
}
