package com.moviefixer.show.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.movie.entity.Movie;
import com.moviefixer.show.dto.LockSeatsRequest;
import com.moviefixer.show.dto.LockSeatsResponse;
import com.moviefixer.show.dto.ShowSeatLayoutResponse;
import com.moviefixer.show.entity.Show;
import com.moviefixer.show.entity.ShowSeat;
import com.moviefixer.show.enums.ShowSeatStatus;
import com.moviefixer.show.repository.ShowRepository;
import com.moviefixer.show.repository.ShowSeatRepository;
import com.moviefixer.theater.entity.Screen;
import com.moviefixer.theater.entity.Seat;
import com.moviefixer.theater.entity.SeatingTier;
import com.moviefixer.theater.entity.Theater;
import com.moviefixer.theater.enums.TierType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShowSeatLockService Unit Tests (Optimistic Query Versioning)")
class ShowSeatLockServiceTest {

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Mock
    private ShowRepository showRepository;

    @InjectMocks
    private ShowSeatLockServiceImpl showSeatLockService;

    private UUID showId;
    private UUID customerId;
    private UUID seatId1;
    private UUID seatId2;
    private Show testShow;
    private ShowSeat showSeat1;
    private ShowSeat showSeat2;
    private UserPrincipal customerPrincipal;

    @BeforeEach
    void setUp() {
        showId = UUID.randomUUID();
        customerId = UUID.randomUUID();
        seatId1 = UUID.randomUUID();
        seatId2 = UUID.randomUUID();

        Movie movie = new Movie();
        movie.setId(UUID.randomUUID());
        movie.setTitle("Interstellar");

        Theater theater = new Theater();
        theater.setName("PVR ICON");

        Screen screen = new Screen();
        screen.setName("Audi 1");
        screen.setTheater(theater);

        testShow = new Show(movie, screen, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), new BigDecimal("300.00"));
        testShow.setId(showId);

        SeatingTier tier = new SeatingTier(UUID.randomUUID(), "Premium", TierType.PREMIUM, new BigDecimal("1.50"), "Desc", true);
        Seat seat1 = new Seat(screen, "A", 1, tier, 0, 0, false, false);
        seat1.setId(seatId1);

        Seat seat2 = new Seat(screen, "A", 2, tier, 0, 1, false, false);
        seat2.setId(seatId2);

        showSeat1 = new ShowSeat(testShow, seat1, new BigDecimal("450.00"));
        showSeat1.setId(UUID.randomUUID());
        showSeat1.setStatus(ShowSeatStatus.AVAILABLE);
        showSeat1.setVersion(0L);

        showSeat2 = new ShowSeat(testShow, seat2, new BigDecimal("450.00"));
        showSeat2.setId(UUID.randomUUID());
        showSeat2.setStatus(ShowSeatStatus.AVAILABLE);
        showSeat2.setVersion(0L);

        customerPrincipal = new UserPrincipal(
                customerId, "Customer", "customer_user", "cust@example.com", "hash",
                AccountStatus.ACTIVE, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }

    @Test
    @DisplayName("getShowSeatLayout returns live availability, versions, and flags seats locked by current user")
    void getShowSeatLayout_success() {
        showSeat1.setStatus(ShowSeatStatus.LOCKED);
        showSeat1.setLockedByUserId(customerId);
        showSeat1.setLockedUntil(LocalDateTime.now().plusMinutes(5));

        when(showRepository.findByIdWithDetails(showId)).thenReturn(Optional.of(testShow));
        when(showSeatRepository.findByShowIdWithSeatDetails(showId)).thenReturn(List.of(showSeat1, showSeat2));

        ShowSeatLayoutResponse response = showSeatLockService.getShowSeatLayout(showId, customerPrincipal);

        assertNotNull(response);
        assertEquals(showId, response.getShowId());
        assertEquals("Interstellar", response.getMovieTitle());
        assertEquals(2, response.getTotalSeats());
        assertEquals(1, response.getAvailableSeats()); // seat 2 is available, seat 1 is locked

        assertTrue(response.getSeats().get(0).isLockedByCurrentUser());
        assertFalse(response.getSeats().get(1).isLockedByCurrentUser());
        assertEquals(0L, response.getSeats().get(0).getVersion());
    }

    @Test
    @DisplayName("lockSeats uses optimistic query versioning CAS to lock seats without DB row locks")
    void lockSeats_optimisticSuccess() {
        LockSeatsRequest request = new LockSeatsRequest(List.of(seatId1, seatId2));

        when(showRepository.existsById(showId)).thenReturn(true);
        when(showSeatRepository.findByShowIdAndSeatIdIn(showId, List.of(seatId1, seatId2)))
                .thenReturn(List.of(showSeat1, showSeat2));
        when(showSeatRepository.atomicLockSeatWithVersion(eq(showSeat1.getId()), eq(0L), eq(customerId), any(), any()))
                .thenReturn(1);
        when(showSeatRepository.atomicLockSeatWithVersion(eq(showSeat2.getId()), eq(0L), eq(customerId), any(), any()))
                .thenReturn(1);

        LockSeatsResponse response = showSeatLockService.lockSeats(showId, request, customerPrincipal);

        assertNotNull(response);
        assertEquals(showId, response.getShowId());
        assertEquals(2, response.getLockedSeatIds().size());
        assertEquals(new BigDecimal("900.00"), response.getTotalPrice()); // 450 + 450
        assertTrue(response.getExpiresInSeconds() > 580); // ~600 seconds

        assertEquals(ShowSeatStatus.LOCKED, showSeat1.getStatus());
        assertEquals(customerId, showSeat1.getLockedByUserId());
        assertEquals(1L, showSeat1.getVersion()); // incremented
        verify(showSeatRepository, times(1))
                .atomicLockSeatWithVersion(eq(showSeat1.getId()), eq(0L), eq(customerId), any(), any());
    }

    @Test
    @DisplayName("lockSeats throws IllegalStateException when version conflict occurs (another customer modified seat)")
    void lockSeats_versionConflict_throwsException() {
        LockSeatsRequest request = new LockSeatsRequest(List.of(seatId1));

        when(showRepository.existsById(showId)).thenReturn(true);
        when(showSeatRepository.findByShowIdAndSeatIdIn(showId, List.of(seatId1)))
                .thenReturn(List.of(showSeat1));
        // Atomic CAS returns 0 rows updated because another customer modified version in the meantime!
        when(showSeatRepository.atomicLockSeatWithVersion(eq(showSeat1.getId()), eq(0L), eq(customerId), any(), any()))
                .thenReturn(0);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> showSeatLockService.lockSeats(showId, request, customerPrincipal));

        assertTrue(ex.getMessage().contains("version conflict"));
    }

    @Test
    @DisplayName("lockSeats throws IllegalStateException when seat is already booked")
    void lockSeats_alreadyBooked_throwsException() {
        showSeat1.setStatus(ShowSeatStatus.BOOKED);
        LockSeatsRequest request = new LockSeatsRequest(List.of(seatId1));

        when(showRepository.existsById(showId)).thenReturn(true);
        when(showSeatRepository.findByShowIdAndSeatIdIn(showId, List.of(seatId1)))
                .thenReturn(List.of(showSeat1));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> showSeatLockService.lockSeats(showId, request, customerPrincipal));

        assertTrue(ex.getMessage().contains("already booked"));
        verify(showSeatRepository, never()).atomicLockSeatWithVersion(any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("lockSeats throws IllegalStateException when seat is locked by another customer")
    void lockSeats_lockedByAnother_throwsException() {
        showSeat1.setStatus(ShowSeatStatus.LOCKED);
        showSeat1.setLockedByUserId(UUID.randomUUID()); // different user
        showSeat1.setLockedUntil(LocalDateTime.now().plusMinutes(8));

        LockSeatsRequest request = new LockSeatsRequest(List.of(seatId1));

        when(showRepository.existsById(showId)).thenReturn(true);
        when(showSeatRepository.findByShowIdAndSeatIdIn(showId, List.of(seatId1)))
                .thenReturn(List.of(showSeat1));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> showSeatLockService.lockSeats(showId, request, customerPrincipal));

        assertTrue(ex.getMessage().contains("locked by another customer"));
    }

    @Test
    @DisplayName("lockSeats throws IllegalArgumentException when selecting more than 10 seats")
    void lockSeats_exceedsMaxLimit_throwsException() {
        List<UUID> elevenSeats = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            elevenSeats.add(UUID.randomUUID());
        }
        LockSeatsRequest request = new LockSeatsRequest(elevenSeats);

        assertThrows(IllegalArgumentException.class,
                () -> showSeatLockService.lockSeats(showId, request, customerPrincipal));
    }

    @Test
    @DisplayName("unlockSeats voluntarily frees seats locked by the current user")
    void unlockSeats_success() {
        showSeat1.setStatus(ShowSeatStatus.LOCKED);
        showSeat1.setLockedByUserId(customerId);

        when(showSeatRepository.findByShowIdAndSeatIdIn(showId, List.of(seatId1)))
                .thenReturn(List.of(showSeat1));
        when(showSeatRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        showSeatLockService.unlockSeats(showId, List.of(seatId1), customerPrincipal);

        assertEquals(ShowSeatStatus.AVAILABLE, showSeat1.getStatus());
        assertNull(showSeat1.getLockedByUserId());
        assertNull(showSeat1.getLockedUntil());
        verify(showSeatRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("releaseExpiredLocks releases seats whose lock timer expired")
    void releaseExpiredLocks_success() {
        when(showSeatRepository.releaseExpiredLocks(any())).thenReturn(5);

        int count = showSeatLockService.releaseExpiredLocks();

        assertEquals(5, count);
        verify(showSeatRepository, times(1)).releaseExpiredLocks(any());
    }
}
