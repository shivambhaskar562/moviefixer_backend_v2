package com.moviefixer.analytics.service;

import com.moviefixer.analytics.dto.*;
import com.moviefixer.analytics.repository.AnalyticsRepository;
import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.location.entity.City;
import com.moviefixer.movie.entity.Movie;
import com.moviefixer.show.entity.Show;
import com.moviefixer.show.enums.ShowSeatStatus;
import com.moviefixer.show.enums.ShowStatus;
import com.moviefixer.show.repository.ShowRepository;
import com.moviefixer.show.repository.ShowSeatRepository;
import com.moviefixer.theater.entity.Screen;
import com.moviefixer.theater.entity.Theater;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Unit Tests (Platform Overview, Multi-Tenant Vendor Metrics & Occupancy)")
class   AnalyticsServiceTest {

    @Mock
    private AnalyticsRepository analyticsRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private UserPrincipal adminPrincipal;
    private UserPrincipal vendorPrincipal;
    private UserPrincipal otherVendorPrincipal;
    private UserPrincipal customerPrincipal;
    private UUID vendorId;
    private UUID otherVendorId;

    @BeforeEach
    void setUp() {
        vendorId = UUID.randomUUID();
        otherVendorId = UUID.randomUUID();

        adminPrincipal = new UserPrincipal(
                UUID.randomUUID(), "Platform Admin", "admin", "admin@moviefixer.com", "pass",
                AccountStatus.ACTIVE, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        vendorPrincipal = new UserPrincipal(
                vendorId, "Cinema Owner 1", "vendor1", "vendor1@cinema.com", "pass",
                AccountStatus.ACTIVE, List.of(new SimpleGrantedAuthority("ROLE_THEATER_VENDOR"))
        );

        otherVendorPrincipal = new UserPrincipal(
                otherVendorId, "Cinema Owner 2", "vendor2", "vendor2@cinema.com", "pass",
                AccountStatus.ACTIVE, List.of(new SimpleGrantedAuthority("ROLE_THEATER_VENDOR"))
        );

        customerPrincipal = new UserPrincipal(
                UUID.randomUUID(), "John Customer", "customer", "customer@gmail.com", "pass",
                AccountStatus.ACTIVE, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
    }

    @Test
    @DisplayName("Platform Overview: Admin successfully fetches system-wide GMV and asset counts")
    void getPlatformOverview_Success() {
        PlatformOverviewResponse mockOverview = new PlatformOverviewResponse(
                new BigDecimal("500000.00"), new BigDecimal("50000.00"), new BigDecimal("450000.00"),
                1200L, 1100L, 100L, 8.33, 2400L, 15L, 45L, 300L, new BigDecimal("208.33")
        );

        when(analyticsRepository.getPlatformOverview()).thenReturn(mockOverview);

        PlatformOverviewResponse result = analyticsService.getPlatformOverview(adminPrincipal);

        assertThat(result).isNotNull();
        assertThat(result.getTotalGrossRevenue()).isEqualByComparingTo("500000.00");
        assertThat(result.getTotalConvenienceFee()).isEqualByComparingTo("50000.00");
        assertThat(result.getTotalVendorPayout()).isEqualByComparingTo("450000.00");
        assertThat(result.getTotalTicketsSold()).isEqualTo(2400L);
        assertThat(result.getCancellationRatePercentage()).isEqualTo(8.33);

        verify(analyticsRepository, times(1)).getPlatformOverview();
    }

    @Test
    @DisplayName("Platform Overview: Non-admin caller receives AccessDeniedException")
    void getPlatformOverview_ForbiddenForNonAdmin() {
        assertThatThrownBy(() -> analyticsService.getPlatformOverview(vendorPrincipal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only platform administrators");

        assertThatThrownBy(() -> analyticsService.getPlatformOverview(customerPrincipal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only platform administrators");

        verifyNoInteractions(analyticsRepository);
    }

    @Test
    @DisplayName("Top Movies: Admin fetches top grossing movies with custom limit")
    void getTopMovies_Success() {
        TopMovieAnalyticsResponse movie = new TopMovieAnalyticsResponse(
                UUID.randomUUID(), "Inception", "https://img.com/inc.jpg", "English",
                50L, 1200L, new BigDecimal("360000.00"), 85.5
        );

        when(analyticsRepository.getTopMovies(5)).thenReturn(List.of(movie));

        List<TopMovieAnalyticsResponse> result = analyticsService.getTopMovies(5, adminPrincipal);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMovieTitle()).isEqualTo("Inception");
        assertThat(result.get(0).getGrossRevenue()).isEqualByComparingTo("360000.00");
        assertThat(result.get(0).getOccupancyRatePercentage()).isEqualTo(85.5);

        verify(analyticsRepository, times(1)).getTopMovies(5);
    }

    @Test
    @DisplayName("City Performance: Admin fetches regional box office breakdown")
    void getCityAnalytics_Success() {
        CityAnalyticsResponse city = new CityAnalyticsResponse(
                1L, "Mumbai", "Maharashtra", 5L, 20L, 100L, 2500L, new BigDecimal("600000.00")
        );

        when(analyticsRepository.getCityAnalytics()).thenReturn(List.of(city));

        List<CityAnalyticsResponse> result = analyticsService.getCityAnalytics(adminPrincipal);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCityName()).isEqualTo("Mumbai");
        assertThat(result.get(0).getGrossRevenue()).isEqualByComparingTo("600000.00");
    }

    @Test
    @DisplayName("Daily Revenue Trend: Admin fetches 14-day time series")
    void getDailyRevenueTrend_Success() {
        DailyRevenueTrendResponse trend = new DailyRevenueTrendResponse(
                "2026-10-10", 45L, 95L, new BigDecimal("28500.00"), new BigDecimal("2850.00")
        );

        when(analyticsRepository.getDailyRevenueTrend(14)).thenReturn(List.of(trend));

        List<DailyRevenueTrendResponse> result = analyticsService.getDailyRevenueTrend(14, adminPrincipal);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDate()).isEqualTo("2026-10-10");
        assertThat(result.get(0).getGrossRevenue()).isEqualByComparingTo("28500.00");
    }

    @Test
    @DisplayName("Vendor Overview: Vendor fetches strictly their scoped box office metrics")
    void getVendorOverview_Success() {
        VendorOverviewResponse mockOverview = new VendorOverviewResponse(
                vendorId, "Cinema Owner 1", 3L, 9L, 60L, 500L,
                new BigDecimal("150000.00"), new BigDecimal("15000.00"), new BigDecimal("135000.00"),
                450L, 90.0, 75.5
        );

        when(analyticsRepository.getVendorOverview(vendorId, "Cinema Owner 1")).thenReturn(mockOverview);

        VendorOverviewResponse result = analyticsService.getVendorOverview(vendorPrincipal);

        assertThat(result).isNotNull();
        assertThat(result.getVendorId()).isEqualTo(vendorId);
        assertThat(result.getGrossBoxOfficeRevenue()).isEqualByComparingTo("150000.00");
        assertThat(result.getNetVendorEarnings()).isEqualByComparingTo("135000.00");
        assertThat(result.getGateCheckInRatePercentage()).isEqualTo(90.0);
        assertThat(result.getOverallOccupancyRatePercentage()).isEqualTo(75.5);

        verify(analyticsRepository, times(1)).getVendorOverview(vendorId, "Cinema Owner 1");
    }

    @Test
    @DisplayName("Vendor Overview: Customer caller receives AccessDeniedException")
    void getVendorOverview_ForbiddenForCustomer() {
        assertThatThrownBy(() -> analyticsService.getVendorOverview(customerPrincipal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only theater vendors and administrators");

        verifyNoInteractions(analyticsRepository);
    }

    @Test
    @DisplayName("Vendor Theaters & Movies: Vendor fetches breakdown for their theaters")
    void getVendorTheatersAndMovies_Success() {
        VendorTheaterAnalyticsResponse theater = new VendorTheaterAnalyticsResponse(
                UUID.randomUUID(), "PVR Phoenix", "Mumbai", 3L, 20L, 180L, new BigDecimal("54000.00"), 78.0
        );
        VendorMovieAnalyticsResponse movie = new VendorMovieAnalyticsResponse(
                UUID.randomUUID(), "Interstellar", "English", 15L, 210L, new BigDecimal("63000.00"), 82.5
        );

        when(analyticsRepository.getVendorTheaters(vendorId)).thenReturn(List.of(theater));
        when(analyticsRepository.getVendorMovies(vendorId)).thenReturn(List.of(movie));

        List<VendorTheaterAnalyticsResponse> theaters = analyticsService.getVendorTheaters(vendorPrincipal);
        List<VendorMovieAnalyticsResponse> movies = analyticsService.getVendorMovies(vendorPrincipal);

        assertThat(theaters).hasSize(1);
        assertThat(theaters.get(0).getTheaterName()).isEqualTo("PVR Phoenix");

        assertThat(movies).hasSize(1);
        assertThat(movies.get(0).getMovieTitle()).isEqualTo("Interstellar");
    }

    @Test
    @DisplayName("Show Occupancy: Vendor fetches real-time capacity and tier yield for their show")
    void getShowOccupancy_Success() {
        UUID showId = UUID.randomUUID();

        Users vendorUser = new Users();
        vendorUser.setId(vendorId);

        City city = new City();
        city.setName("Mumbai");

        Theater theater = new Theater();
        theater.setName("PVR Phoenix");
        theater.setAddress("Lower Parel");
        theater.setCity(city);
        theater.setVendor(vendorUser);

        Screen screen = new Screen();
        screen.setTheater(theater);
        screen.setName("Audi 1");
        screen.setTotalSeats(100);

        Movie movie = new Movie();
        movie.setTitle("Avatar: The Way of Water");

        Show show = new Show(movie, screen, LocalDateTime.now(), LocalDateTime.now().plusHours(3), new BigDecimal("250.00"));
        show.setStatus(ShowStatus.SCHEDULED);

        when(showRepository.findByIdWithDetails(showId)).thenReturn(Optional.of(show));
        when(showSeatRepository.countByShowIdAndStatus(showId, ShowSeatStatus.BOOKED)).thenReturn(80L);
        when(showSeatRepository.countByShowIdAndStatus(showId, ShowSeatStatus.AVAILABLE)).thenReturn(15L);
        when(showSeatRepository.countByShowIdAndStatus(showId, ShowSeatStatus.LOCKED)).thenReturn(5L);

        TierBreakdownResponse vipTier = new TierBreakdownResponse(
                "VIP", "VIP", new BigDecimal("500.00"), 20L, 20L, 100.0, new BigDecimal("10000.00")
        );
        TierBreakdownResponse standardTier = new TierBreakdownResponse(
                "Standard", "STANDARD", new BigDecimal("250.00"), 80L, 60L, 75.0, new BigDecimal("15000.00")
        );

        when(analyticsRepository.getShowTierBreakdowns(showId)).thenReturn(List.of(vipTier, standardTier));

        ShowOccupancyAnalyticsResponse result = analyticsService.getShowOccupancy(showId, vendorPrincipal);

        assertThat(result).isNotNull();
        assertThat(result.getMovieTitle()).isEqualTo("Avatar: The Way of Water");
        assertThat(result.getTheaterName()).isEqualTo("PVR Phoenix");
        assertThat(result.getTotalCapacity()).isEqualTo(100L);
        assertThat(result.getBookedSeatsCount()).isEqualTo(80L);
        assertThat(result.getAvailableSeatsCount()).isEqualTo(15L);
        assertThat(result.getLockedSeatsCount()).isEqualTo(5L);
        assertThat(result.getOccupancyPercentage()).isEqualTo(80.0);
        assertThat(result.getGrossRevenue()).isEqualByComparingTo("25000.00");
        assertThat(result.getTierBreakdowns()).hasSize(2);
    }

    @Test
    @DisplayName("Show Occupancy: Vendor is blocked when accessing another vendor's show")
    void getShowOccupancy_VendorMismatch_ThrowsAccessDenied() {
        UUID showId = UUID.randomUUID();

        Users otherVendor = new Users();
        otherVendor.setId(otherVendorId);

        Theater theater = new Theater();
        theater.setName("INOX Megaplex");
        theater.setCity(new City());
        theater.setVendor(otherVendor);

        Screen screen = new Screen();
        screen.setTheater(theater);
        screen.setName("Screen 1");
        screen.setTotalSeats(100);

        Show show = new Show(new Movie(), screen, LocalDateTime.now(), LocalDateTime.now().plusHours(2), new BigDecimal("200.00"));

        when(showRepository.findByIdWithDetails(showId)).thenReturn(Optional.of(show));

        assertThatThrownBy(() -> analyticsService.getShowOccupancy(showId, vendorPrincipal))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not authorized to view analytics for this theater");

        verifyNoInteractions(showSeatRepository);
        verify(analyticsRepository, never()).getShowTierBreakdowns(any());
    }

    @Test
    @DisplayName("Show Occupancy: Show not found throws ResourceNotFoundException")
    void getShowOccupancy_NotFound_ThrowsResourceNotFound() {
        UUID showId = UUID.randomUUID();
        when(showRepository.findByIdWithDetails(showId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> analyticsService.getShowOccupancy(showId, adminPrincipal))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Show not found with id");
    }
}
