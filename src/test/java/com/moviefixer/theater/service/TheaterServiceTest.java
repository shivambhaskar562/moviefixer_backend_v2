package com.moviefixer.theater.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.location.entity.City;
import com.moviefixer.location.entity.Country;
import com.moviefixer.location.entity.State;
import com.moviefixer.location.repository.CityRepository;
import com.moviefixer.theater.dto.CreateTheaterRequest;
import com.moviefixer.theater.dto.TheaterResponse;
import com.moviefixer.theater.dto.TheaterSummaryResponse;
import com.moviefixer.theater.dto.UpdateTheaterRequest;
import com.moviefixer.theater.entity.Theater;
import com.moviefixer.theater.repository.ScreenRepository;
import com.moviefixer.theater.repository.TheaterRepository;
import com.moviefixer.users.entity.Role;
import com.moviefixer.users.entity.Users;
import com.moviefixer.users.enums.AccountStatus;
import com.moviefixer.users.enums.USER_ROLE;
import com.moviefixer.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TheaterService Unit Tests")
class TheaterServiceTest {

    @Mock
    private TheaterRepository theaterRepository;

    @Mock
    private ScreenRepository screenRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TheaterServiceImpl theaterService;

    private UUID theaterId;
    private UUID cityId;
    private UUID vendorId;
    private City testCity;
    private Users testVendor;
    private Theater testTheater;
    private UserPrincipal vendorPrincipal;

    @BeforeEach
    void setUp() {
        theaterId = UUID.randomUUID();
        cityId = UUID.randomUUID();
        vendorId = UUID.randomUUID();

        Country india = new Country("IN", "India", "+91");
        State maharashtra = new State("Maharashtra", "MH", india);
        testCity = new City("Mumbai", maharashtra);
        testCity.setId(cityId);

        testVendor = new Users();
        testVendor.setId(vendorId);
        testVendor.setUsername("cinepolis_vendor");
        testVendor.setEmail("vendor@cinepolis.com");
        testVendor.setName("Cinepolis Manager");
        testVendor.setRoles(Set.of(new Role(USER_ROLE.THEATER_VENDOR, "Vendor")));

        testTheater = new Theater(
                "PVR Phoenix Marketcity",
                "LBS Marg, Kurla West",
                testCity,
                testVendor,
                "+919876543210",
                "phoenix@pvr.com",
                new BigDecimal("19.0860"),
                new BigDecimal("72.8880")
        );
        testTheater.setId(theaterId);
        testTheater.setActive(true);
        testTheater.setCreatedAt(Instant.now());
        testTheater.setUpdatedAt(Instant.now());

        vendorPrincipal = new UserPrincipal(
                vendorId,
                "Cinepolis Manager",
                "cinepolis_vendor",
                "vendor@cinepolis.com",
                "hash",
                AccountStatus.ACTIVE,
                List.of(new SimpleGrantedAuthority("ROLE_THEATER_VENDOR"))
        );
    }

    @Test
    @DisplayName("createTheater successfully registers theater for vendor")
    void createTheater_success() {
        CreateTheaterRequest request = new CreateTheaterRequest(
                "PVR Phoenix Marketcity",
                "LBS Marg, Kurla West",
                cityId,
                vendorId,
                "+919876543210",
                "phoenix@pvr.com",
                new BigDecimal("19.0860"),
                new BigDecimal("72.8880")
        );

        when(cityRepository.findById(cityId)).thenReturn(Optional.of(testCity));
        when(theaterRepository.existsByNameIgnoreCaseAndCityId("PVR Phoenix Marketcity", cityId)).thenReturn(false);
        when(userRepository.findById(vendorId)).thenReturn(Optional.of(testVendor));
        when(theaterRepository.save(any(Theater.class))).thenAnswer(invocation -> {
            Theater t = invocation.getArgument(0);
            t.setId(theaterId);
            t.setCreatedAt(Instant.now());
            t.setUpdatedAt(Instant.now());
            return t;
        });

        TheaterResponse response = theaterService.createTheater(request, vendorPrincipal);

        assertNotNull(response);
        assertEquals(theaterId, response.getId());
        assertEquals("PVR Phoenix Marketcity", response.getName());
        assertEquals("Mumbai", response.getCityName());
        assertEquals("cinepolis_vendor", response.getVendorName());
        verify(theaterRepository, times(1)).save(any(Theater.class));
    }

    @Test
    @DisplayName("createTheater throws IllegalArgumentException on duplicate name in city")
    void createTheater_duplicateName_throwsException() {
        CreateTheaterRequest request = new CreateTheaterRequest(
                "PVR Phoenix Marketcity", "Address", cityId, vendorId, null, null, null, null
        );

        when(cityRepository.findById(cityId)).thenReturn(Optional.of(testCity));
        when(theaterRepository.existsByNameIgnoreCaseAndCityId("PVR Phoenix Marketcity", cityId)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> theaterService.createTheater(request, vendorPrincipal));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    @DisplayName("createTheater throws ResourceNotFoundException when city is not found")
    void createTheater_cityNotFound_throwsException() {
        CreateTheaterRequest request = new CreateTheaterRequest(
                "PVR Phoenix Marketcity", "Address", cityId, vendorId, null, null, null, null
        );

        when(cityRepository.findById(cityId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> theaterService.createTheater(request, vendorPrincipal));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    @DisplayName("getTheaterById returns TheaterResponse when found")
    void getTheaterById_found() {
        when(theaterRepository.findByIdWithDetails(theaterId)).thenReturn(Optional.of(testTheater));
        when(screenRepository.countByTheaterIdAndActiveTrue(theaterId)).thenReturn(5L);

        TheaterResponse response = theaterService.getTheaterById(theaterId);

        assertNotNull(response);
        assertEquals(theaterId, response.getId());
        assertEquals("PVR Phoenix Marketcity", response.getName());
        assertEquals(5L, response.getTotalScreens());
    }

    @Test
    @DisplayName("getTheaterById throws ResourceNotFoundException when not found")
    void getTheaterById_notFound_throwsException() {
        UUID unknownId = UUID.randomUUID();
        when(theaterRepository.findByIdWithDetails(unknownId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> theaterService.getTheaterById(unknownId));
    }

    @Test
    @DisplayName("updateTheater allows owner vendor to update theater")
    void updateTheater_ownerSuccess() {
        UpdateTheaterRequest updateRequest = new UpdateTheaterRequest();
        updateRequest.setName("PVR Luxe Phoenix");
        updateRequest.setContactPhone("+919999999999");

        when(theaterRepository.findByIdWithDetails(theaterId)).thenReturn(Optional.of(testTheater));
        when(theaterRepository.save(any(Theater.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(screenRepository.countByTheaterIdAndActiveTrue(theaterId)).thenReturn(5L);

        TheaterResponse response = theaterService.updateTheater(theaterId, updateRequest, vendorPrincipal);

        assertNotNull(response);
        assertEquals("PVR Luxe Phoenix", response.getName());
        assertEquals("+919999999999", response.getContactPhone());
        verify(theaterRepository, times(1)).save(testTheater);
    }

    @Test
    @DisplayName("updateTheater throws AccessDeniedException when caller is not owner and not admin")
    void updateTheater_unauthorizedCaller_throwsAccessDeniedException() {
        UserPrincipal otherVendor = new UserPrincipal(
                UUID.randomUUID(), "Other", "other_vendor", "other@cinepolis.com", "hash",
                AccountStatus.ACTIVE, List.of(new SimpleGrantedAuthority("ROLE_THEATER_VENDOR"))
        );

        when(theaterRepository.findByIdWithDetails(theaterId)).thenReturn(Optional.of(testTheater));

        UpdateTheaterRequest updateRequest = new UpdateTheaterRequest();
        updateRequest.setName("Hijacked Name");

        assertThrows(AccessDeniedException.class, () -> theaterService.updateTheater(theaterId, updateRequest, otherVendor));
        verify(theaterRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteTheater soft-archives theater")
    void deleteTheater_softArchives() {
        when(theaterRepository.findByIdWithDetails(theaterId)).thenReturn(Optional.of(testTheater));
        when(theaterRepository.save(any(Theater.class))).thenAnswer(invocation -> invocation.getArgument(0));

        theaterService.deleteTheater(theaterId, vendorPrincipal);

        assertFalse(testTheater.isActive());
        verify(theaterRepository, times(1)).save(testTheater);
    }

    @Test
    @DisplayName("searchTheaters returns paginated summary responses")
    void searchTheaters_returnsPagedSummaries() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Theater> theaterPage = new PageImpl<>(List.of(testTheater), pageable, 1);

        when(theaterRepository.searchTheaters(eq(cityId), eq("Phoenix"), eq(pageable))).thenReturn(theaterPage);
        when(screenRepository.countByTheaterIdAndActiveTrue(theaterId)).thenReturn(5L);

        Page<TheaterSummaryResponse> result = theaterService.searchTheaters(cityId, "Phoenix", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        TheaterSummaryResponse summary = result.getContent().get(0);
        assertEquals("PVR Phoenix Marketcity", summary.getName());
        assertEquals(5L, summary.getTotalScreens());
    }
}
