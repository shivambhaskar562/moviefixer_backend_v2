package com.moviefixer.booking.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.booking.dto.TicketResponse;
import com.moviefixer.booking.dto.ValidateQrRequest;
import com.moviefixer.booking.dto.ValidateQrResponse;

import com.moviefixer.booking.entity.Booking;
import com.moviefixer.booking.entity.Ticket;
import com.moviefixer.booking.enums.BookingStatus;
import com.moviefixer.booking.repository.TicketRepository;
import com.moviefixer.movie.entity.Movie;
import com.moviefixer.show.entity.Show;
import com.moviefixer.show.entity.ShowSeat;
import com.moviefixer.theater.entity.Screen;
import com.moviefixer.theater.entity.Seat;
import com.moviefixer.theater.entity.SeatingTier;
import com.moviefixer.theater.entity.Theater;
import com.moviefixer.theater.enums.TierType;
import com.moviefixer.users.entity.Users;
import com.moviefixer.users.enums.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TicketValidationService Unit Tests (Gate Staff Scan & Duplicate Entry Prevention)")
class TicketValidationServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private QrCodeService qrCodeService;

    @InjectMocks
    private TicketValidationServiceImpl ticketValidationService;

    private UserPrincipal gateStaffPrincipal;
    private Ticket testTicket;
    private Booking testBooking;
    private static final String VALID_PAYLOAD = "encoded_payload.valid_signature";
    private static final String TICKET_NUMBER = "TKT-20261010-XYZ123-01";

    @BeforeEach
    void setUp() {
        UUID staffId = UUID.randomUUID();
        gateStaffPrincipal = new UserPrincipal(
                staffId,
                "gatestaff1",
                "staff@moviefixer.com",
                "password",
                AccountStatus.ACTIVE,
                List.of(new SimpleGrantedAuthority("ROLE_THEATER_TICKET_STAFF"))
        );

        Users customer = new Users();
        customer.setId(UUID.randomUUID());
        customer.setName("Alice Cooper");

        Theater theater = new Theater();
        theater.setName("PVR Cinemas Phoenix");

        Screen screen = new Screen();
        screen.setName("Screen 2");
        screen.setTheater(theater);

        Movie movie = new Movie();
        movie.setTitle("Interstellar");

        Show show = new Show();
        show.setId(UUID.randomUUID());
        show.setMovie(movie);
        show.setScreen(screen);
        show.setStartTime(LocalDateTime.now().plusHours(1));

        SeatingTier tier = new SeatingTier("VIP", TierType.VIP, BigDecimal.valueOf(2.0), "VIP Recliner");
        Seat seat = new Seat();
        seat.setRowIdentifier("A");
        seat.setSeatNumber(1);
        seat.setTier(tier);

        ShowSeat showSeat = new ShowSeat();
        showSeat.setSeat(seat);
        showSeat.setPrice(BigDecimal.valueOf(500.00));

        testBooking = new Booking();
        testBooking.setId(UUID.randomUUID());
        testBooking.setBookingReference("MF-20261010-XYZ123");
        testBooking.setUser(customer);
        testBooking.setShow(show);
        testBooking.setStatus(BookingStatus.CONFIRMED);

        testTicket = new Ticket();
        testTicket.setId(UUID.randomUUID());
        testTicket.setTicketNumber(TICKET_NUMBER);
        testTicket.setBooking(testBooking);
        testTicket.setShowSeat(showSeat);
        testTicket.setCheckedIn(false);
    }

    @Test
    @DisplayName("Should successfully admit passenger on valid unscanned ticket")
    void shouldAdmitValidUnscannedTicket() {
        ValidateQrRequest request = new ValidateQrRequest(VALID_PAYLOAD);

        when(qrCodeService.verifyAndExtractTicketNumber(VALID_PAYLOAD)).thenReturn(TICKET_NUMBER);
        when(ticketRepository.findByTicketNumberWithDetails(TICKET_NUMBER)).thenReturn(Optional.of(testTicket));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));

        ValidateQrResponse response = ticketValidationService.validateAndAdmit(request, gateStaffPrincipal);

        assertNotNull(response);
        assertTrue(response.isValid());
        assertTrue(response.isAdmitted());
        assertFalse(response.isAlreadyCheckedIn());
        assertEquals("A1", response.getSeatLabel());
        assertEquals("Interstellar", response.getMovieTitle());
        assertEquals("Alice Cooper", response.getCustomerName());
        assertNotNull(response.getCheckedInAt());
        assertEquals(gateStaffPrincipal.getId(), response.getCheckedInByUserId());

        assertTrue(testTicket.isCheckedIn());
        verify(ticketRepository, times(1)).save(testTicket);
    }

    @Test
    @DisplayName("Should prevent duplicate admission and sound alert when ticket was already scanned")
    void shouldRejectDuplicateAdmissionWhenAlreadyCheckedIn() {
        LocalDateTime firstScan = LocalDateTime.now().minusMinutes(15);
        UUID firstStaffId = UUID.randomUUID();
        testTicket.setCheckedIn(true);
        testTicket.setCheckedInAt(firstScan);
        testTicket.setCheckedInByUserId(firstStaffId);

        ValidateQrRequest request = new ValidateQrRequest(VALID_PAYLOAD);

        when(qrCodeService.verifyAndExtractTicketNumber(VALID_PAYLOAD)).thenReturn(TICKET_NUMBER);
        when(ticketRepository.findByTicketNumberWithDetails(TICKET_NUMBER)).thenReturn(Optional.of(testTicket));

        ValidateQrResponse response = ticketValidationService.validateAndAdmit(request, gateStaffPrincipal);

        assertNotNull(response);
        assertTrue(response.isValid());
        assertFalse(response.isAdmitted()); // ADMISSION DENIED
        assertTrue(response.isAlreadyCheckedIn()); // DUPLICATE SCAN
        assertEquals(firstScan, response.getCheckedInAt());
        assertEquals(firstStaffId, response.getCheckedInByUserId());
        assertTrue(response.getMessage().contains("ALREADY ADMITTED"));

        // Must NOT update the ticket check-in time or save
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Should deny admission if ticket belongs to a CANCELLED booking")
    void shouldDenyAdmissionIfBookingCancelled() {
        testBooking.setStatus(BookingStatus.CANCELLED);
        ValidateQrRequest request = new ValidateQrRequest(VALID_PAYLOAD);

        when(qrCodeService.verifyAndExtractTicketNumber(VALID_PAYLOAD)).thenReturn(TICKET_NUMBER);
        when(ticketRepository.findByTicketNumberWithDetails(TICKET_NUMBER)).thenReturn(Optional.of(testTicket));

        ValidateQrResponse response = ticketValidationService.validateAndAdmit(request, gateStaffPrincipal);

        assertNotNull(response);
        assertFalse(response.isValid());
        assertFalse(response.isAdmitted());
        assertTrue(response.getMessage().contains("Admission Denied"));
        assertTrue(response.getMessage().contains("CANCELLED"));

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    @DisplayName("Should throw SecurityException when QR payload signature is forged or tampered")
    void shouldThrowSecurityExceptionWhenQrPayloadIsForged() {
        String forgedPayload = "tampered_data.bad_signature";
        ValidateQrRequest request = new ValidateQrRequest(forgedPayload);

        when(qrCodeService.verifyAndExtractTicketNumber(forgedPayload))
                .thenThrow(new SecurityException("Cryptographic verification failed: QR pass is invalid"));

        assertThrows(SecurityException.class, () -> ticketValidationService.validateAndAdmit(request, gateStaffPrincipal));
        verify(ticketRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when QR payload is null or blank")
    void shouldThrowIllegalArgumentExceptionWhenQrPayloadIsBlank() {
        assertThrows(IllegalArgumentException.class, () ->
                ticketValidationService.validateAndAdmit(new ValidateQrRequest(""), gateStaffPrincipal));

        assertThrows(IllegalArgumentException.class, () ->
                ticketValidationService.validateAndAdmit(new ValidateQrRequest(null), gateStaffPrincipal));
    }

    @Test
    @DisplayName("Should retrieve ticket details by ticket number")
    void shouldGetTicketByNumberSuccessfully() {
        when(ticketRepository.findByTicketNumberWithDetails(TICKET_NUMBER)).thenReturn(Optional.of(testTicket));

        TicketResponse response = ticketValidationService.getTicketByNumber(TICKET_NUMBER, gateStaffPrincipal);

        assertNotNull(response);
        assertEquals(TICKET_NUMBER, response.getTicketNumber());
        assertEquals("A", response.getSeatRow());
        assertEquals(1, response.getSeatNumber());
        assertEquals("A1", response.getSeatLabel());
        assertEquals("VIP", response.getTierName());
        assertEquals(BigDecimal.valueOf(500.00), response.getPrice());
        assertFalse(response.isCheckedIn());
    }
}

