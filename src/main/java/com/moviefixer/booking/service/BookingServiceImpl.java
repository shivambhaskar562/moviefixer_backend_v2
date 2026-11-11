package com.moviefixer.booking.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.booking.dto.*;
import com.moviefixer.booking.entity.Booking;
import com.moviefixer.booking.entity.Ticket;
import com.moviefixer.booking.enums.BookingStatus;
import com.moviefixer.booking.repository.BookingRepository;
import com.moviefixer.booking.repository.TicketRepository;
import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.show.entity.Show;
import com.moviefixer.show.entity.ShowSeat;
import com.moviefixer.show.enums.ShowSeatStatus;
import com.moviefixer.show.enums.ShowStatus;
import com.moviefixer.show.repository.ShowRepository;
import com.moviefixer.show.repository.ShowSeatRepository;
import com.moviefixer.theater.entity.Screen;
import com.moviefixer.theater.entity.Seat;
import com.moviefixer.theater.entity.Theater;
import com.moviefixer.users.entity.Users;
import com.moviefixer.users.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int LOCK_WINDOW_MINUTES = 10;

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final UserRepository userRepository;
    private final QrCodeService qrCodeService;

    private final double convenienceFeePercentage;
    private final int minHoursBeforeShowForCancellation;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            TicketRepository ticketRepository,
            ShowRepository showRepository,
            ShowSeatRepository showSeatRepository,
            UserRepository userRepository,
            QrCodeService qrCodeService,
            @Value("${app.booking.convenience-fee-percentage:10.0}") double convenienceFeePercentage,
            @Value("${app.booking.cancellation-min-hours-before-show:2}") int minHoursBeforeShowForCancellation) {
        this.bookingRepository = bookingRepository;
        this.ticketRepository = ticketRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.userRepository = userRepository;
        this.qrCodeService = qrCodeService;
        this.convenienceFeePercentage = convenienceFeePercentage;
        this.minHoursBeforeShowForCancellation = minHoursBeforeShowForCancellation;
    }

    @Override
    @Transactional
    public InitiateBookingResponse initiateBooking(InitiateBookingRequest request, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User must be authenticated to initiate booking");
        }

        Users user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Show show = showRepository.findByIdWithDetails(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", request.getShowId()));

        if (show.getStatus() != ShowStatus.SCHEDULED) {
            throw new IllegalStateException("Cannot book tickets for a show that is " + show.getStatus());
        }

        List<ShowSeat> selectedShowSeats = showSeatRepository.findByShowIdAndSeatIdIn(show.getId(), request.getSeatIds());
        if (selectedShowSeats.size() != request.getSeatIds().size()) {
            throw new ResourceNotFoundException("One or more selected seats were not found for this show");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(LOCK_WINDOW_MINUTES);
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<String> seatLabels = new ArrayList<>();

        // Validate and ensure seats are locked for this customer via Optimistic Query Versioning CAS
        for (ShowSeat ss : selectedShowSeats) {
            Seat seat = ss.getSeat();
            String seatLabel = seat.getRowIdentifier() + seat.getSeatNumber();
            seatLabels.add(seatLabel);

            if (ss.getStatus() == ShowSeatStatus.BOOKED) {
                throw new IllegalStateException("Seat " + seatLabel + " is already booked and unavailable");
            }

            // If not already locked by this user, attempt atomic OCC lock
            boolean isLockedByMe = (ss.getStatus() == ShowSeatStatus.LOCKED)
                    && ss.getLockedUntil() != null
                    && ss.getLockedUntil().isAfter(now)
                    && currentUser.getId().equals(ss.getLockedByUserId());

            if (!isLockedByMe) {
                int rows = showSeatRepository.atomicLockSeatWithVersion(
                        ss.getId(),
                        ss.getVersion(),
                        currentUser.getId(),
                        expiresAt,
                        now
                );

                if (rows == 0) {
                    throw new IllegalStateException("Seat " + seatLabel + " was concurrently modified or locked by another customer (version conflict)");
                }
                ss.setVersion(ss.getVersion() + 1);
                ss.setStatus(ShowSeatStatus.LOCKED);
                ss.setLockedByUserId(currentUser.getId());
                ss.setLockedUntil(expiresAt);
            }

            totalAmount = totalAmount.add(ss.getPrice());
        }

        // Financial Calculations
        BigDecimal convenienceFee = totalAmount.multiply(BigDecimal.valueOf(convenienceFeePercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal netAmount = totalAmount.add(convenienceFee).subtract(discountAmount);

        String bookingReference = generateUniqueBookingReference();

        Booking booking = new Booking(
                bookingReference,
                user,
                show,
                totalAmount,
                convenienceFee,
                discountAmount,
                netAmount,
                BookingStatus.INITIATED,
                expiresAt
        );

        int seq = 1;
        for (ShowSeat ss : selectedShowSeats) {
            String ticketNumber = generateUniqueTicketNumber(bookingReference, seq++);
            Ticket ticket = new Ticket(booking, ss, ticketNumber, "PENDING_CONFIRMATION");
            booking.addTicket(ticket);
        }

        Booking savedBooking = bookingRepository.save(booking);
        long expiresInSeconds = Duration.between(now, expiresAt).getSeconds();

        log.info("Booking session initiated: ref={} for user={} with {} seats",
                bookingReference, currentUser.getUsername(), selectedShowSeats.size());

        Screen screen = show.getScreen();
        Theater theater = (screen != null) ? screen.getTheater() : null;

        return new InitiateBookingResponse(
                savedBooking.getId(),
                savedBooking.getBookingReference(),
                show.getId(),
                show.getMovie().getTitle(),
                (theater != null) ? theater.getName() : null,
                (screen != null) ? screen.getName() : null,
                show.getStartTime(),
                seatLabels,
                totalAmount,
                convenienceFee,
                discountAmount,
                netAmount,
                savedBooking.getStatus(),
                savedBooking.getExpiresAt(),
                expiresInSeconds
        );
    }

    @Override
    @Transactional
    public BookingResponse confirmBooking(UUID bookingId, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User must be authenticated to confirm booking");
        }

        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        validateBookingAccess(booking, currentUser);

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return mapToBookingResponse(booking);
        }

        if (booking.getStatus() != BookingStatus.INITIATED) {
            throw new IllegalStateException("Booking cannot be confirmed from status: " + booking.getStatus());
        }

        LocalDateTime now = LocalDateTime.now();
        if (booking.getExpiresAt().isBefore(now)) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            throw new IllegalStateException("Booking reservation session has expired. Please select seats again.");
        }

        // Execute non-blocking Query Versioning CAS: Transition seats to BOOKED
        for (Ticket ticket : booking.getTickets()) {
            ShowSeat ss = ticket.getShowSeat();
            // Fetch fresh state to inspect current version
            ShowSeat freshSs = showSeatRepository.findById(ss.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("ShowSeat", "id", ss.getId()));

            int rowsUpdated = showSeatRepository.atomicBookSeatWithVersion(
                    freshSs.getId(),
                    freshSs.getVersion(),
                    currentUser.getId()
            );

            if (rowsUpdated == 0) {
                Seat seat = freshSs.getSeat();
                String seatLabel = seat.getRowIdentifier() + seat.getSeatNumber();
                log.warn("Version conflict detected during confirmation on seat {}. Current version={}",
                        seatLabel, freshSs.getVersion());
                throw new IllegalStateException("Seat " + seatLabel + " version conflict: seat was modified or lock expired. Booking aborted (zero DB row locks).");
            }

            freshSs.setStatus(ShowSeatStatus.BOOKED);
            freshSs.setVersion(freshSs.getVersion() + 1);

            // Generate tamper-proof HMAC-SHA256 digital QR payload
            String qrPayload = qrCodeService.generateSignedQrPayload(ticket);
            ticket.setQrPayload(qrPayload);
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking savedBooking = bookingRepository.save(booking);

        log.info("Booking CONFIRMED successfully: ref={} for user={}",
                savedBooking.getBookingReference(), currentUser.getUsername());

        return mapToBookingResponse(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponse directBooking(DirectBookingRequest request, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User must be authenticated to book tickets");
        }

        Users user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        Show show = showRepository.findByIdWithDetails(request.getShowId())
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", request.getShowId()));

        if (show.getStatus() != ShowStatus.SCHEDULED) {
            throw new IllegalStateException("Show is not available for booking. Status: " + show.getStatus());
        }

        List<ShowSeat> selectedShowSeats = showSeatRepository.findByShowIdAndSeatIdIn(show.getId(), request.getSeatIds());
        if (selectedShowSeats.size() != request.getSeatIds().size()) {
            throw new ResourceNotFoundException("One or more selected seats were not found for this show");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ShowSeat ss : selectedShowSeats) {
            Seat seat = ss.getSeat();
            String seatLabel = seat.getRowIdentifier() + seat.getSeatNumber();

            // Direct atomic compare-and-swap (CAS) using query versioning
            int rowsUpdated = showSeatRepository.atomicBookSeatWithVersion(
                    ss.getId(),
                    ss.getVersion(),
                    currentUser.getId()
            );

            if (rowsUpdated == 0) {
                throw new IllegalStateException("Seat " + seatLabel + " is unavailable or was modified by another customer (version conflict).");
            }

            ss.setStatus(ShowSeatStatus.BOOKED);
            ss.setVersion(ss.getVersion() + 1);
            totalAmount = totalAmount.add(ss.getPrice());
        }

        BigDecimal convenienceFee = totalAmount.multiply(BigDecimal.valueOf(convenienceFeePercentage))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal netAmount = totalAmount.add(convenienceFee).subtract(discountAmount);

        String bookingReference = generateUniqueBookingReference();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(LOCK_WINDOW_MINUTES);

        Booking booking = new Booking(
                bookingReference,
                user,
                show,
                totalAmount,
                convenienceFee,
                discountAmount,
                netAmount,
                BookingStatus.CONFIRMED,
                expiresAt
        );

        int seq = 1;
        for (ShowSeat ss : selectedShowSeats) {
            String ticketNumber = generateUniqueTicketNumber(bookingReference, seq++);
            Ticket ticket = new Ticket(booking, ss, ticketNumber, "PENDING");
            String qrPayload = qrCodeService.generateSignedQrPayload(ticket);
            ticket.setQrPayload(qrPayload);
            booking.addTicket(ticket);
        }

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Direct booking confirmed: ref={} for user={}", bookingReference, currentUser.getUsername());

        return mapToBookingResponse(savedBooking);
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(UUID bookingId, UserPrincipal currentUser) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        validateBookingAccess(booking, currentUser);
        return mapToBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingResponse> getMyBookings(UserPrincipal currentUser, Pageable pageable) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User must be authenticated");
        }

        Page<Booking> bookingsPage = bookingRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId(), pageable);
        return bookingsPage.map(this::mapToBookingResponse);
    }

    @Override
    @Transactional
    public CancelBookingResponse cancelBooking(UUID bookingId, UserPrincipal currentUser) {
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        validateBookingAccess(booking, currentUser);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only CONFIRMED bookings can be cancelled. Current status: " + booking.getStatus());
        }

        Show show = booking.getShow();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime showStart = show.getStartTime();

        if (now.isAfter(showStart)) {
            throw new IllegalStateException("Cannot cancel booking for a show that has already started or completed");
        }

        Duration duration = Duration.between(now, showStart);
        long hoursRemaining = duration.toHours();

        if (hoursRemaining < minHoursBeforeShowForCancellation) {
            throw new IllegalStateException(String.format(
                    "Cancellation is strictly prohibited within %d hours of showtime. Hours remaining: %d",
                    minHoursBeforeShowForCancellation, hoursRemaining
            ));
        }

        BigDecimal refundPercentage;
        if (hoursRemaining >= 24) {
            refundPercentage = BigDecimal.valueOf(1.00); // 100% refund of seat prices
        } else {
            refundPercentage = BigDecimal.valueOf(0.75); // 75% refund of seat prices
        }

        BigDecimal refundAmount = booking.getTotalAmount().multiply(refundPercentage).setScale(2, RoundingMode.HALF_UP);
        BigDecimal cancellationPenalty = booking.getTotalAmount().subtract(refundAmount);

        // Atomically release booked seats back to AVAILABLE inventory
        for (Ticket ticket : booking.getTickets()) {
            ShowSeat ss = ticket.getShowSeat();
            showSeatRepository.atomicReleaseBookedSeat(ss.getId());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        log.info("Booking cancelled: ref={}, refundAmount={}, hoursRemaining={}",
                booking.getBookingReference(), refundAmount, hoursRemaining);

        return new CancelBookingResponse(
                booking.getId(),
                booking.getBookingReference(),
                booking.getStatus(),
                booking.getTotalAmount(),
                refundAmount,
                cancellationPenalty,
                String.format("Booking cancelled successfully. %.0f%% ticket refund processed. Platform convenience fee is non-refundable.",
                        refundPercentage.doubleValue() * 100)
        );
    }

    @Override
    @Transactional
    public int expireUnconfirmedBookings() {
        LocalDateTime now = LocalDateTime.now();
        List<Booking> expiredBookings = bookingRepository.findExpiredBookings(BookingStatus.INITIATED, now);

        int count = 0;
        for (Booking booking : expiredBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
            for (Ticket ticket : booking.getTickets()) {
                ShowSeat ss = ticket.getShowSeat();
                showSeatRepository.atomicReleaseLockedSeat(ss.getId(), booking.getUser().getId());
            }
            bookingRepository.save(booking);
            count++;
        }

        if (count > 0) {
            log.info("Reclaimed {} expired unconfirmed checkout bookings", count);
        }
        return count;
    }

    private void validateBookingAccess(Booking booking, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User must be authenticated");
        }

        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());
        boolean hasPrivilegedRole = currentUser.getAuthorities().stream().anyMatch(a ->
                "ROLE_ADMIN".equals(a.getAuthority()) ||
                "ROLE_ADMIN_SUPPORT".equals(a.getAuthority()) ||
                "ROLE_THEATER_MANAGER".equals(a.getAuthority()) ||
                "ROLE_THEATER_TICKET_STAFF".equals(a.getAuthority()) ||
                "ROLE_THEATER_VENDOR".equals(a.getAuthority())
        );

        if (!isOwner && !hasPrivilegedRole) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to view or manage this booking"
            );
        }
    }

    private BookingResponse mapToBookingResponse(Booking booking) {
        Show show = booking.getShow();
        Screen screen = (show != null) ? show.getScreen() : null;
        Theater theater = (screen != null) ? screen.getTheater() : null;

        List<TicketResponse> ticketResponses = new ArrayList<>();
        if (booking.getTickets() != null) {
            for (Ticket t : booking.getTickets()) {
                ShowSeat ss = t.getShowSeat();
                Seat st = (ss != null) ? ss.getSeat() : null;
                String tierName = (st != null && st.getTier() != null) ? st.getTier().getName() : "STANDARD";
                String row = (st != null) ? st.getRowIdentifier() : "";
                Integer num = (st != null) ? st.getSeatNumber() : 0;
                String label = row + num;
                BigDecimal price = (ss != null) ? ss.getPrice() : BigDecimal.ZERO;

                ticketResponses.add(new TicketResponse(
                        t.getId(),
                        t.getTicketNumber(),
                        row,
                        num,
                        label,
                        tierName,
                        price,
                        t.getQrPayload(),
                        t.isCheckedIn(),
                        t.getCheckedInAt()
                ));
            }
        }

        return new BookingResponse(
                booking.getId(),
                booking.getBookingReference(),
                (show != null) ? show.getId() : null,
                (show != null && show.getMovie() != null) ? show.getMovie().getTitle() : null,
                (show != null && show.getMovie() != null) ? show.getMovie().getPosterUrl() : null,
                (theater != null) ? theater.getName() : null,
                (theater != null) ? theater.getAddress() : null,
                (screen != null) ? screen.getName() : null,
                (screen != null) ? screen.getScreenType() : null,
                (show != null) ? show.getStartTime() : null,
                (show != null) ? show.getEndTime() : null,
                booking.getTotalAmount(),
                booking.getConvenienceFee(),
                booking.getDiscountAmount(),
                booking.getNetAmount(),
                booking.getStatus(),
                booking.getExpiresAt(),
                booking.getCreatedAt(),
                ticketResponses
        );
    }

    private String generateUniqueBookingReference() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String ref;
        do {
            ref = "MF-" + datePrefix + "-" + randomAlphanumeric(6);
        } while (bookingRepository.existsByBookingReference(ref));
        return ref;
    }

    private String generateUniqueTicketNumber(String bookingReference, int seq) {
        return "TKT-" + bookingReference.replace("MF-", "") + String.format("-%02d", seq);
    }

    private String randomAlphanumeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
