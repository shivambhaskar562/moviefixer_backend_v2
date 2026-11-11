package com.moviefixer.booking.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.booking.dto.*;
import com.moviefixer.booking.entity.Booking;
import com.moviefixer.booking.entity.Ticket;
import com.moviefixer.booking.enums.BookingStatus;
import com.moviefixer.booking.repository.BookingRepository;
import com.moviefixer.booking.repository.TicketRepository;
import com.moviefixer.movie.entity.Movie;
import com.moviefixer.show.entity.Show;
import com.moviefixer.show.entity.ShowSeat;
import com.moviefixer.show.enums.ShowStatus;
import com.moviefixer.show.repository.ShowRepository;
import com.moviefixer.show.repository.ShowSeatRepository;
import com.moviefixer.theater.entity.Screen;
import com.moviefixer.theater.entity.Seat;
import com.moviefixer.theater.entity.SeatingTier;
import com.moviefixer.theater.entity.Theater;
import com.moviefixer.theater.enums.ScreenType;
import com.moviefixer.theater.enums.TierType;
import com.moviefixer.users.entity.Users;
import com.moviefixer.users.enums.AccountStatus;
import com.moviefixer.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests (Optimistic Query Versioning & Zero DB Row Locking)")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QrCodeService qrCodeService;

    private BookingServiceImpl bookingService;

    private UUID customerId;
    private UUID showId;
    private UUID seatId1;
    private UUID seatId2;
    private Users testUser;
    private UserPrincipal customerPrincipal;
    private Show testShow;
    private ShowSeat showSeat1;
    private ShowSeat showSeat2;

    @BeforeEach
    void setUp() {
        bookingService = new BookingServiceImpl(
                bookingRepository,
                ticketRepository,
                showRepository,
                showSeatRepository,
                userRepository,
                qrCodeService,
                10.0, // 10% convenience fee
                2     // 2 hours minimum before show for cancellation
        );

        customerId = UUID.randomUUID();
        showId = UUID.randomUUID();
        seatId1 = UUID.randomUUID();
        seatId2 = UUID.randomUUID();

        testUser = new Users();
        testUser.setId(customerId);
        testUser.setName("John Customer");
        testUser.setUsername("johncust");
        testUser.setEmail("john@example.com");

        customerPrincipal = new UserPrincipal(
                customerId,
                "John Customer",
                "johncust",
                "john@example.com",
                "password",
                AccountStatus.ACTIVE,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        Theater theater = new Theater();
        theater.setId(UUID.randomUUID());
        theater.setName("PVR Cinemas Phoenix");

        Screen screen = new Screen();
        screen.setId(UUID.randomUUID());
        screen.setName("Screen 1");
        screen.setScreenType(ScreenType.IMAX_3D);
        screen.setTheater(theater);

        Movie movie = new Movie();
        movie.setId(UUID.randomUUID());
        movie.setTitle("Inception");

        testShow = new Show();
        testShow.setId(showId);
        testShow.setMovie(movie);
        testShow.setScreen(screen);
        testShow.setStartTime(LocalDateTime.now().plusDays(2));
        testShow.setEndTime(LocalDateTime.now().plusDays(2).plusHours(2).plusMinutes(30));
        testShow.setBasePrice(BigDecimal.valueOf(250.00));
        testShow.setStatus(ShowStatus.SCHEDULED);

        SeatingTier tier = new SeatingTier("EXECUTIVE", TierType.EXECUTIVE, BigDecimal.valueOf(1.20), "Executive", true);


        Seat seat1 = new Seat();
        seat1.setId(seatId1);
        seat1.setRowIdentifier("D");
        seat1.setSeatNumber(5);
        seat1.setTier(tier);

        Seat seat2 = new Seat();
        seat2.setId(seatId2);
        seat2.setRowIdentifier("D");
        seat2.setSeatNumber(6);
        seat2.setTier(tier);

        showSeat1 = new ShowSeat(testShow, seat1, BigDecimal.valueOf(300.00));
        showSeat1.setId(UUID.randomUUID());
        showSeat1.setVersion(0L);

        showSeat2 = new ShowSeat(testShow, seat2, BigDecimal.valueOf(300.00));
        showSeat2.setId(UUID.randomUUID());
        showSeat2.setVersion(0L);
    }

    @Test
    @DisplayName("Should successfully initiate booking session with atomic OCC seat locks and 10-minute hold")
    void shouldInitiateBookingSuccessfully() {
        InitiateBookingRequest request = new InitiateBookingRequest(showId, List.of(seatId1, seatId2));

        when(userRepository.findById(customerId)).thenReturn(Optional.of(testUser));
        when(showRepository.findByIdWithDetails(showId)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findByShowIdAndSeatIdIn(eq(showId), any())).thenReturn(List.of(showSeat1, showSeat2));
        when(showSeatRepository.atomicLockSeatWithVersion(any(), any(), any(), any(), any())).thenReturn(1);
        when(bookingRepository.existsByBookingReference(any())).thenReturn(false);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        InitiateBookingResponse response = bookingService.initiateBooking(request, customerPrincipal);

        assertNotNull(response);
        assertNotNull(response.getBookingId());
        assertTrue(response.getBookingReference().startsWith("MF-"));
        assertEquals("Inception", response.getMovieTitle());
        assertEquals("PVR Cinemas Phoenix", response.getTheaterName());
        assertEquals(2, response.getSeatLabels().size());
        assertEquals(BigDecimal.valueOf(600.00), response.getTotalAmount());
        // 10% fee on 600.00 = 60.00
        assertEquals(new BigDecimal("60.00"), response.getConvenienceFee());
        assertEquals(new BigDecimal("660.00"), response.getNetAmount());
        assertEquals(BookingStatus.INITIATED, response.getStatus());

        // Verify atomic OCC locking queries were invoked for both seats
        verify(showSeatRepository, times(2)).atomicLockSeatWithVersion(any(), any(), eq(customerId), any(), any());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should confirm booking with atomic query versioning CAS and issue cryptographically signed tickets")
    void shouldConfirmBookingSuccessfullyWithQueryVersioning() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = new Booking(
                "MF-20261010-XYZ123",
                testUser,
                testShow,
                BigDecimal.valueOf(600.00),
                BigDecimal.valueOf(60.00),
                BigDecimal.ZERO,
                BigDecimal.valueOf(660.00),
                BookingStatus.INITIATED,
                LocalDateTime.now().plusMinutes(8)
        );
        booking.setId(bookingId);

        Ticket ticket1 = new Ticket(booking, showSeat1, "TKT-20261010-XYZ123-01", "PENDING");
        Ticket ticket2 = new Ticket(booking, showSeat2, "TKT-20261010-XYZ123-02", "PENDING");
        booking.addTicket(ticket1);
        booking.addTicket(ticket2);

        when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(booking));
        when(showSeatRepository.findById(showSeat1.getId())).thenReturn(Optional.of(showSeat1));
        when(showSeatRepository.findById(showSeat2.getId())).thenReturn(Optional.of(showSeat2));
        // Atomic CAS query returns 1 (success, version matched)
        when(showSeatRepository.atomicBookSeatWithVersion(any(), any(), eq(customerId))).thenReturn(1);
        when(qrCodeService.generateSignedQrPayload(any())).thenReturn("signed_payload_valid_base64.signature");
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        BookingResponse response = bookingService.confirmBooking(bookingId, customerPrincipal);

        assertNotNull(response);
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        assertEquals(2, response.getTickets().size());
        assertEquals("signed_payload_valid_base64.signature", response.getTickets().get(0).getQrPayload());

        // Verify atomic CAS updates were called
        verify(showSeatRepository, times(2)).atomicBookSeatWithVersion(any(), any(), eq(customerId));
        verify(qrCodeService, times(2)).generateSignedQrPayload(any());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("Should reject confirmation when query versioning detects concurrent conflict (zero row locking)")
    void shouldRejectConfirmationWhenVersionConflictDetected() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = new Booking(
                "MF-20261010-XYZ123",
                testUser,
                testShow,
                BigDecimal.valueOf(600.00),
                BigDecimal.valueOf(60.00),
                BigDecimal.ZERO,
                BigDecimal.valueOf(660.00),
                BookingStatus.INITIATED,
                LocalDateTime.now().plusMinutes(8)
        );
        booking.setId(bookingId);

        Ticket ticket1 = new Ticket(booking, showSeat1, "TKT-20261010-XYZ123-01", "PENDING");
        booking.addTicket(ticket1);

        when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(booking));
        when(showSeatRepository.findById(showSeat1.getId())).thenReturn(Optional.of(showSeat1));
        // Atomic CAS query returns 0 -> Version conflict!
        when(showSeatRepository.atomicBookSeatWithVersion(any(), any(), eq(customerId))).thenReturn(0);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> bookingService.confirmBooking(bookingId, customerPrincipal));

        assertTrue(ex.getMessage().contains("version conflict"));
        assertTrue(ex.getMessage().contains("Booking aborted"));
    }

    @Test
    @DisplayName("Should reject confirmation if booking session expired")
    void shouldRejectConfirmationWhenBookingSessionExpired() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = new Booking(
                "MF-20261010-XYZ123",
                testUser,
                testShow,
                BigDecimal.valueOf(600.00),
                BigDecimal.valueOf(60.00),
                BigDecimal.ZERO,
                BigDecimal.valueOf(660.00),
                BookingStatus.INITIATED,
                LocalDateTime.now().minusMinutes(2) // Expired 2 minutes ago
        );
        booking.setId(bookingId);

        when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(booking));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> bookingService.confirmBooking(bookingId, customerPrincipal));

        assertTrue(ex.getMessage().contains("expired"));
        assertEquals(BookingStatus.EXPIRED, booking.getStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("Should cancel confirmed booking with 100% refund when > 24 hours before showtime")
    void shouldCancelBookingWith100PercentRefundWhenOver24Hours() {
        UUID bookingId = UUID.randomUUID();
        // Show is in 48 hours
        testShow.setStartTime(LocalDateTime.now().plusHours(48));

        Booking booking = new Booking(
                "MF-20261010-XYZ123",
                testUser,
                testShow,
                BigDecimal.valueOf(600.00),
                BigDecimal.valueOf(60.00),
                BigDecimal.ZERO,
                BigDecimal.valueOf(660.00),
                BookingStatus.CONFIRMED,
                LocalDateTime.now().plusDays(1)
        );
        booking.setId(bookingId);

        Ticket ticket1 = new Ticket(booking, showSeat1, "TKT-01", "signed");
        booking.addTicket(ticket1);

        when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(booking));
        when(showSeatRepository.atomicReleaseBookedSeat(any())).thenReturn(1);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        CancelBookingResponse response = bookingService.cancelBooking(bookingId, customerPrincipal);

        assertNotNull(response);
        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        assertEquals(new BigDecimal("600.00"), response.getRefundAmount());
        assertEquals(new BigDecimal("0.00"), response.getCancellationPenalty());

        verify(showSeatRepository, times(1)).atomicReleaseBookedSeat(showSeat1.getId());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    @DisplayName("Should cancel confirmed booking with 75% refund when between 2 and 24 hours before showtime")
    void shouldCancelBookingWith75PercentRefundWhenBetween2And24Hours() {
        UUID bookingId = UUID.randomUUID();
        // Show is in 10 hours
        testShow.setStartTime(LocalDateTime.now().plusHours(10));

        Booking booking = new Booking(
                "MF-20261010-XYZ123",
                testUser,
                testShow,
                BigDecimal.valueOf(600.00),
                BigDecimal.valueOf(60.00),
                BigDecimal.ZERO,
                BigDecimal.valueOf(660.00),
                BookingStatus.CONFIRMED,
                LocalDateTime.now().plusDays(1)
        );
        booking.setId(bookingId);

        Ticket ticket1 = new Ticket(booking, showSeat1, "TKT-01", "signed");
        booking.addTicket(ticket1);

        when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(booking));
        when(showSeatRepository.atomicReleaseBookedSeat(any())).thenReturn(1);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));

        CancelBookingResponse response = bookingService.cancelBooking(bookingId, customerPrincipal);

        assertNotNull(response);
        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        // 75% of 600.00 = 450.00
        assertEquals(new BigDecimal("450.00"), response.getRefundAmount());
        assertEquals(new BigDecimal("150.00"), response.getCancellationPenalty());

        verify(showSeatRepository, times(1)).atomicReleaseBookedSeat(showSeat1.getId());
    }

    @Test
    @DisplayName("Should prohibit cancellation within 2 hours of showtime")
    void shouldProhibitCancellationWithin2HoursOfShowtime() {
        UUID bookingId = UUID.randomUUID();
        // Show is in 1 hour
        testShow.setStartTime(LocalDateTime.now().plusMinutes(60));

        Booking booking = new Booking(
                "MF-20261010-XYZ123",
                testUser,
                testShow,
                BigDecimal.valueOf(600.00),
                BigDecimal.valueOf(60.00),
                BigDecimal.ZERO,
                BigDecimal.valueOf(660.00),
                BookingStatus.CONFIRMED,
                LocalDateTime.now().plusDays(1)
        );
        booking.setId(bookingId);

        when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(booking));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> bookingService.cancelBooking(bookingId, customerPrincipal));

        assertTrue(ex.getMessage().contains("strictly prohibited"));
    }

    @Test
    @DisplayName("Should reclaim expired initiated bookings and restore seats")
    void shouldReclaimExpiredInitiatedBookings() {
        Booking expiredBooking = new Booking(
                "MF-20261010-EXP001",
                testUser,
                testShow,
                BigDecimal.valueOf(300.00),
                BigDecimal.valueOf(30.00),
                BigDecimal.ZERO,
                BigDecimal.valueOf(330.00),
                BookingStatus.INITIATED,
                LocalDateTime.now().minusMinutes(5)
        );
        Ticket ticket = new Ticket(expiredBooking, showSeat1, "TKT-EXP-01", "PENDING");
        expiredBooking.addTicket(ticket);

        when(bookingRepository.findExpiredBookings(eq(BookingStatus.INITIATED), any()))
                .thenReturn(List.of(expiredBooking));
        when(showSeatRepository.atomicReleaseLockedSeat(any(), any())).thenReturn(1);

        int reclaimed = bookingService.expireUnconfirmedBookings();

        assertEquals(1, reclaimed);
        assertEquals(BookingStatus.EXPIRED, expiredBooking.getStatus());
        verify(showSeatRepository, times(1)).atomicReleaseLockedSeat(eq(showSeat1.getId()), eq(customerId));
        verify(bookingRepository, times(1)).save(expiredBooking);
    }

    @Test
    @DisplayName("Should successfully execute direct atomic booking with query versioning and issue tickets")
    void shouldDirectBookingSuccessfullyWithQueryVersioning() {
        DirectBookingRequest request = new DirectBookingRequest(showId, List.of(seatId1, seatId2));

        when(userRepository.findById(customerId)).thenReturn(Optional.of(testUser));
        when(showRepository.findByIdWithDetails(showId)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findByShowIdAndSeatIdIn(eq(showId), any())).thenReturn(List.of(showSeat1, showSeat2));
        when(showSeatRepository.atomicBookSeatWithVersion(any(), any(), eq(customerId))).thenReturn(1);
        when(bookingRepository.existsByBookingReference(any())).thenReturn(false);
        when(qrCodeService.generateSignedQrPayload(any())).thenReturn("signed_qr_direct_booking.signature");
        when(bookingRepository.save(any(Booking.class))).thenAnswer(i -> {
            Booking b = i.getArgument(0);
            b.setId(UUID.randomUUID());
            return b;
        });

        BookingResponse response = bookingService.directBooking(request, customerPrincipal);

        assertNotNull(response);
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        assertEquals("Inception", response.getMovieTitle());
        assertEquals(2, response.getTickets().size());
        assertEquals(BigDecimal.valueOf(600.00), response.getTotalAmount());
        assertEquals(new BigDecimal("660.00"), response.getNetAmount());

        verify(showSeatRepository, times(2)).atomicBookSeatWithVersion(any(), any(), eq(customerId));
        verify(qrCodeService, times(2)).generateSignedQrPayload(any());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    @DisplayName("Should return booking details when requested by owning customer")
    void shouldGetBookingByIdSuccessfullyWhenAuthorized() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = new Booking(
                "MF-20261010-XYZ123",
                testUser,
                testShow,
                BigDecimal.valueOf(300.00),
                BigDecimal.valueOf(30.00),
                BigDecimal.ZERO,
                BigDecimal.valueOf(330.00),
                BookingStatus.CONFIRMED,
                LocalDateTime.now().plusHours(2)
        );
        booking.setId(bookingId);

        when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.getBookingById(bookingId, customerPrincipal);

        assertNotNull(response);
        assertEquals("MF-20261010-XYZ123", response.getBookingReference());
        assertEquals("Inception", response.getMovieTitle());
    }

    @Test
    @DisplayName("Should throw AccessDeniedException when another customer tries to view booking")
    void shouldThrowAccessDeniedExceptionWhenUserDoesNotOwnBooking() {
        UUID bookingId = UUID.randomUUID();
        Users differentUser = new Users();
        differentUser.setId(UUID.randomUUID());
        differentUser.setName("Other User");

        Booking booking = new Booking(
                "MF-20261010-XYZ123",
                differentUser,
                testShow,
                BigDecimal.valueOf(300.00),
                BigDecimal.valueOf(30.00),
                BigDecimal.ZERO,
                BigDecimal.valueOf(330.00),
                BookingStatus.CONFIRMED,
                LocalDateTime.now().plusHours(2)
        );
        booking.setId(bookingId);

        when(bookingRepository.findByIdWithDetails(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class, () -> bookingService.getBookingById(bookingId, customerPrincipal));
    }

    @Test
    @DisplayName("Should return paginated customer booking history")
    void shouldGetMyBookingsSuccessfullyWithPagination() {
        Pageable pageable = PageRequest.of(0, 5);
        Booking booking = new Booking(
                "MF-20261010-XYZ123",
                testUser,
                testShow,
                BigDecimal.valueOf(300.00),
                BigDecimal.valueOf(30.00),
                BigDecimal.ZERO,
                BigDecimal.valueOf(330.00),
                BookingStatus.CONFIRMED,
                LocalDateTime.now().plusHours(2)
        );
        booking.setId(UUID.randomUUID());

        Page<Booking> page = new PageImpl<>(List.of(booking), pageable, 1);
        when(bookingRepository.findByUserIdOrderByCreatedAtDesc(customerId, pageable)).thenReturn(page);

        Page<BookingResponse> responsePage = bookingService.getMyBookings(customerPrincipal, pageable);

        assertNotNull(responsePage);
        assertEquals(1, responsePage.getTotalElements());
        assertEquals("MF-20261010-XYZ123", responsePage.getContent().get(0).getBookingReference());
    }
}

