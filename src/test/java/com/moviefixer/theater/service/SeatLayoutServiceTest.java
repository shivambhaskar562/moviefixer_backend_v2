package com.moviefixer.theater.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.theater.dto.*;
import com.moviefixer.theater.entity.Screen;
import com.moviefixer.theater.entity.Seat;
import com.moviefixer.theater.entity.SeatingTier;
import com.moviefixer.theater.entity.Theater;
import com.moviefixer.theater.enums.TierType;
import com.moviefixer.theater.repository.ScreenRepository;
import com.moviefixer.theater.repository.SeatRepository;
import com.moviefixer.theater.repository.SeatingTierRepository;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeatLayoutService Unit Tests")
class SeatLayoutServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private ScreenRepository screenRepository;

    @Mock
    private SeatingTierRepository seatingTierRepository;

    @InjectMocks
    private SeatLayoutServiceImpl seatLayoutService;

    private UUID screenId;
    private UUID vendorId;
    private UUID vipTierId;
    private UUID standardTierId;
    private Screen testScreen;
    private SeatingTier vipTier;
    private SeatingTier standardTier;
    private UserPrincipal vendorPrincipal;

    @BeforeEach
    void setUp() {
        screenId = UUID.randomUUID();
        vendorId = UUID.randomUUID();
        vipTierId = UUID.randomUUID();
        standardTierId = UUID.randomUUID();

        Users vendor = new Users();
        vendor.setId(vendorId);

        Theater theater = new Theater();
        theater.setId(UUID.randomUUID());
        theater.setName("Cinepolis Grand");
        theater.setVendor(vendor);

        testScreen = new Screen();
        testScreen.setId(screenId);
        testScreen.setName("Audi 1");
        testScreen.setTheater(theater);
        testScreen.setTotalSeats(0);

        vipTier = new SeatingTier(vipTierId, "VIP", TierType.VIP, new BigDecimal("2.00"), "Recliners", true);
        standardTier = new SeatingTier(standardTierId, "Standard", TierType.STANDARD, new BigDecimal("1.00"), "Standard", true);

        vendorPrincipal = new UserPrincipal(
                vendorId, "Manager", "vendor", "vendor@cinepolis.com", "hash",
                AccountStatus.ACTIVE, List.of(new SimpleGrantedAuthority("ROLE_THEATER_VENDOR"))
        );
    }

    @Test
    @DisplayName("configureBatchLayout successfully creates custom seat grid and updates screen capacity")
    void configureBatchLayout_success() {
        SeatItemDto s1 = new SeatItemDto("A", 1, vipTierId, 0, 0, false, false, true);
        SeatItemDto s2 = new SeatItemDto("A", 2, vipTierId, 0, 1, false, false, true);
        SeatItemDto s3 = new SeatItemDto("B", 1, standardTierId, 1, 0, true, true, true);

        BatchSeatLayoutRequest request = new BatchSeatLayoutRequest(List.of(s1, s2, s3));

        when(screenRepository.findByIdWithTheater(screenId)).thenReturn(Optional.of(testScreen));
        when(seatingTierRepository.findAllById(Set.of(vipTierId, standardTierId))).thenReturn(List.of(vipTier, standardTier));
        doNothing().when(seatRepository).deleteByScreenId(screenId);
        when(seatRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScreenLayoutResponse response = seatLayoutService.configureBatchLayout(screenId, request, vendorPrincipal);

        assertNotNull(response);
        assertEquals(screenId, response.getScreenId());
        assertEquals(3, response.getTotalSeats());
        assertEquals(3, response.getSeats().size());
        assertEquals(2, response.getTiers().size());
        assertEquals(2, response.getMaxGridRow());
        assertEquals(2, response.getMaxGridColumn());

        verify(seatRepository, times(1)).deleteByScreenId(screenId);
        verify(screenRepository, times(1)).save(testScreen);
        assertEquals(3, testScreen.getTotalSeats());
    }

    @Test
    @DisplayName("configureBatchLayout throws IllegalArgumentException on duplicate row + seat number")
    void configureBatchLayout_duplicateSeat_throwsException() {
        SeatItemDto s1 = new SeatItemDto("A", 1, vipTierId, 0, 0, false, false, true);
        SeatItemDto s2 = new SeatItemDto("A", 1, vipTierId, 0, 1, false, false, true); // Duplicate A:1

        BatchSeatLayoutRequest request = new BatchSeatLayoutRequest(List.of(s1, s2));

        when(screenRepository.findByIdWithTheater(screenId)).thenReturn(Optional.of(testScreen));
        when(seatingTierRepository.findAllById(Set.of(vipTierId))).thenReturn(List.of(vipTier));

        assertThrows(IllegalArgumentException.class, () -> seatLayoutService.configureBatchLayout(screenId, request, vendorPrincipal));
        verify(seatRepository, never()).deleteByScreenId(any());
    }

    @Test
    @DisplayName("configureBatchLayout throws IllegalArgumentException on duplicate grid coordinates")
    void configureBatchLayout_duplicateGridCoord_throwsException() {
        SeatItemDto s1 = new SeatItemDto("A", 1, vipTierId, 0, 0, false, false, true);
        SeatItemDto s2 = new SeatItemDto("A", 2, vipTierId, 0, 0, false, false, true); // Duplicate grid (0,0)

        BatchSeatLayoutRequest request = new BatchSeatLayoutRequest(List.of(s1, s2));

        when(screenRepository.findByIdWithTheater(screenId)).thenReturn(Optional.of(testScreen));
        when(seatingTierRepository.findAllById(Set.of(vipTierId))).thenReturn(List.of(vipTier));

        assertThrows(IllegalArgumentException.class, () -> seatLayoutService.configureBatchLayout(screenId, request, vendorPrincipal));
    }

    @Test
    @DisplayName("generateGridLayout automatically generates rectangular seat matrix with tier overrides")
    void generateGridLayout_success() {
        GenerateGridRequest request = new GenerateGridRequest(
                3, // Rows A, B, C
                4, // Seats 1, 2, 3, 4 per row
                standardTierId,
                Map.of("A", vipTierId), // Row A is VIP
                List.of(2), // Column 2 is aisle
                List.of("A1") // A1 is wheelchair accessible
        );

        when(screenRepository.findByIdWithTheater(screenId)).thenReturn(Optional.of(testScreen));
        when(seatingTierRepository.findById(standardTierId)).thenReturn(Optional.of(standardTier));
        when(seatingTierRepository.findById(vipTierId)).thenReturn(Optional.of(vipTier));
        doNothing().when(seatRepository).deleteByScreenId(screenId);
        when(seatRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScreenLayoutResponse response = seatLayoutService.generateGridLayout(screenId, request, vendorPrincipal);

        assertNotNull(response);
        assertEquals(12, response.getTotalSeats()); // 3 * 4 = 12
        assertEquals(12, response.getSeats().size());
        assertEquals(3, response.getMaxGridRow());
        assertEquals(4, response.getMaxGridColumn());

        // Check A1 properties (VIP, Wheelchair)
        SeatResponse a1 = response.getSeats().stream().filter(s -> s.getRowIdentifier().equals("A") && s.getSeatNumber() == 1).findFirst().orElseThrow();
        assertEquals(TierType.VIP, a1.getTierType());
        assertTrue(a1.isWheelchairAccessible());

        // Check aisle column 2
        SeatResponse b2 = response.getSeats().stream().filter(s -> s.getRowIdentifier().equals("B") && s.getSeatNumber() == 2).findFirst().orElseThrow();
        assertTrue(b2.isAisle());
        assertEquals(TierType.STANDARD, b2.getTierType());

        verify(screenRepository, times(1)).save(testScreen);
        assertEquals(12, testScreen.getTotalSeats());
    }

    @Test
    @DisplayName("toggleSeatStatus successfully updates active status and recalculates screen seat count")
    void toggleSeatStatus_success() {
        UUID seatId = UUID.randomUUID();
        Seat seat = new Seat(testScreen, "A", 1, standardTier, 0, 0, false, false);
        seat.setId(seatId);
        seat.setActive(true);

        when(seatRepository.findByIdWithDetails(seatId)).thenReturn(Optional.of(seat));
        when(seatRepository.save(any(Seat.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(seatRepository.countByScreenIdAndActiveTrue(screenId)).thenReturn(99L);

        SeatResponse response = seatLayoutService.toggleSeatStatus(seatId, false, vendorPrincipal);

        assertNotNull(response);
        assertFalse(response.isActive());
        verify(seatRepository, times(1)).save(seat);
        verify(screenRepository, times(1)).save(testScreen);
        assertEquals(99, testScreen.getTotalSeats());
    }
}
