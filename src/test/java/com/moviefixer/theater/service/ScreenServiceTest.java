package com.moviefixer.theater.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.location.entity.City;
import com.moviefixer.theater.dto.CreateScreenRequest;
import com.moviefixer.theater.dto.ScreenResponse;
import com.moviefixer.theater.dto.UpdateScreenRequest;
import com.moviefixer.theater.entity.Screen;
import com.moviefixer.theater.entity.Theater;
import com.moviefixer.theater.enums.ScreenType;
import com.moviefixer.theater.repository.ScreenRepository;
import com.moviefixer.theater.repository.TheaterRepository;
import com.moviefixer.users.entity.Users;
import com.moviefixer.users.enums.AccountStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScreenService Unit Tests")
class ScreenServiceTest {

    @Mock
    private ScreenRepository screenRepository;

    @Mock
    private TheaterRepository theaterRepository;

    @InjectMocks
    private ScreenServiceImpl screenService;

    private UUID theaterId;
    private UUID screenId;
    private UUID vendorId;
    private Theater testTheater;
    private Screen testScreen;
    private UserPrincipal vendorPrincipal;

    @BeforeEach
    void setUp() {
        theaterId = UUID.randomUUID();
        screenId = UUID.randomUUID();
        vendorId = UUID.randomUUID();

        Users vendor = new Users();
        vendor.setId(vendorId);
        vendor.setUsername("theater_owner");

        testTheater = new Theater("Inox Megaplex", "Inorbit Mall", new City("Mumbai", null), vendor, null, null, BigDecimal.ZERO, BigDecimal.ZERO);
        testTheater.setId(theaterId);

        testScreen = new Screen(testTheater, "Screen 1 - IMAX 3D", ScreenType.IMAX_3D, "IMAX 12-Channel", 250);
        testScreen.setId(screenId);
        testScreen.setActive(true);
        testScreen.setCreatedAt(Instant.now());
        testScreen.setUpdatedAt(Instant.now());

        vendorPrincipal = new UserPrincipal(
                vendorId, "Owner", "theater_owner", "owner@inox.com", "hash",
                AccountStatus.ACTIVE, List.of(new SimpleGrantedAuthority("ROLE_THEATER_VENDOR"))
        );
    }

    @Test
    @DisplayName("createScreen successfully registers screen in theater")
    void createScreen_success() {
        CreateScreenRequest request = new CreateScreenRequest("Screen 1 - IMAX 3D", ScreenType.IMAX_3D, "IMAX 12-Channel", 250);

        when(theaterRepository.findByIdWithDetails(theaterId)).thenReturn(Optional.of(testTheater));
        when(screenRepository.existsByTheaterIdAndNameIgnoreCase(theaterId, "Screen 1 - IMAX 3D")).thenReturn(false);
        when(screenRepository.save(any(Screen.class))).thenAnswer(invocation -> {
            Screen s = invocation.getArgument(0);
            s.setId(screenId);
            s.setCreatedAt(Instant.now());
            s.setUpdatedAt(Instant.now());
            return s;
        });

        ScreenResponse response = screenService.createScreen(theaterId, request, vendorPrincipal);

        assertNotNull(response);
        assertEquals(screenId, response.getId());
        assertEquals("Screen 1 - IMAX 3D", response.getName());
        assertEquals(ScreenType.IMAX_3D, response.getScreenType());
        assertEquals("IMAX 12-Channel", response.getSoundSystem());
        verify(screenRepository, times(1)).save(any(Screen.class));
    }

    @Test
    @DisplayName("createScreen throws IllegalArgumentException when screen name already exists in theater")
    void createScreen_duplicateName_throwsException() {
        CreateScreenRequest request = new CreateScreenRequest("Screen 1 - IMAX 3D", ScreenType.IMAX_3D, null, 250);

        when(theaterRepository.findByIdWithDetails(theaterId)).thenReturn(Optional.of(testTheater));
        when(screenRepository.existsByTheaterIdAndNameIgnoreCase(theaterId, "Screen 1 - IMAX 3D")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> screenService.createScreen(theaterId, request, vendorPrincipal));
        verify(screenRepository, never()).save(any());
    }

    @Test
    @DisplayName("createScreen throws AccessDeniedException when caller is not theater owner")
    void createScreen_unauthorizedCaller_throwsAccessDeniedException() {
        UserPrincipal otherVendor = new UserPrincipal(
                UUID.randomUUID(), "Other", "other", "other@cinepolis.com", "hash",
                AccountStatus.ACTIVE, List.of(new SimpleGrantedAuthority("ROLE_THEATER_VENDOR"))
        );

        when(theaterRepository.findByIdWithDetails(theaterId)).thenReturn(Optional.of(testTheater));

        CreateScreenRequest request = new CreateScreenRequest("Audi 2", ScreenType.STANDARD_2D, null, 150);

        assertThrows(AccessDeniedException.class, () -> screenService.createScreen(theaterId, request, otherVendor));
        verify(screenRepository, never()).save(any());
    }

    @Test
    @DisplayName("getScreenById returns ScreenResponse when found")
    void getScreenById_found() {
        when(screenRepository.findByIdWithTheater(screenId)).thenReturn(Optional.of(testScreen));

        ScreenResponse response = screenService.getScreenById(screenId);

        assertNotNull(response);
        assertEquals(screenId, response.getId());
        assertEquals("Screen 1 - IMAX 3D", response.getName());
        assertEquals("Inox Megaplex", response.getTheaterName());
    }

    @Test
    @DisplayName("getScreenById throws ResourceNotFoundException when not found")
    void getScreenById_notFound_throwsException() {
        UUID unknownId = UUID.randomUUID();
        when(screenRepository.findByIdWithTheater(unknownId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> screenService.getScreenById(unknownId));
    }

    @Test
    @DisplayName("updateScreen modifies projection format and sound system")
    void updateScreen_success() {
        UpdateScreenRequest updateRequest = new UpdateScreenRequest();
        updateRequest.setScreenType(ScreenType.FOUR_DX);
        updateRequest.setSoundSystem("Dolby Atmos 9.1.4");

        when(screenRepository.findByIdWithTheater(screenId)).thenReturn(Optional.of(testScreen));
        when(screenRepository.save(any(Screen.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ScreenResponse response = screenService.updateScreen(screenId, updateRequest, vendorPrincipal);

        assertNotNull(response);
        assertEquals(ScreenType.FOUR_DX, response.getScreenType());
        assertEquals("Dolby Atmos 9.1.4", response.getSoundSystem());
        verify(screenRepository, times(1)).save(testScreen);
    }

    @Test
    @DisplayName("deleteScreen soft-archives screen")
    void deleteScreen_softArchives() {
        when(screenRepository.findByIdWithTheater(screenId)).thenReturn(Optional.of(testScreen));
        when(screenRepository.save(any(Screen.class))).thenAnswer(invocation -> invocation.getArgument(0));

        screenService.deleteScreen(screenId, vendorPrincipal);

        assertFalse(testScreen.isActive());
        verify(screenRepository, times(1)).save(testScreen);
    }
}
