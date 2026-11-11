package com.moviefixer.show.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.location.entity.City;
import com.moviefixer.location.repository.CityRepository;
import com.moviefixer.movie.entity.Movie;
import com.moviefixer.movie.repository.MovieRepository;
import com.moviefixer.show.dto.*;
import com.moviefixer.show.entity.Show;
import com.moviefixer.show.repository.ShowRepository;
import com.moviefixer.show.repository.ShowSeatRepository;
import com.moviefixer.theater.entity.Screen;
import com.moviefixer.theater.entity.Seat;
import com.moviefixer.theater.entity.SeatingTier;
import com.moviefixer.theater.entity.Theater;
import com.moviefixer.theater.enums.ScreenType;
import com.moviefixer.theater.enums.TierType;
import com.moviefixer.theater.repository.ScreenRepository;
import com.moviefixer.theater.repository.SeatRepository;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ShowService Unit Tests")
class ShowServiceTest {

    @Mock
    private ShowRepository showRepository;

    @Mock
    private ShowSeatRepository showSeatRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ScreenRepository screenRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private TheaterRepository theaterRepository;

    @InjectMocks
    private ShowServiceImpl showService;

    private UUID movieId;
    private UUID screenId;
    private UUID theaterId;
    private UUID cityId;
    private UUID vendorId;
    private Movie testMovie;
    private Screen testScreen;
    private Theater testTheater;
    private UserPrincipal vendorPrincipal;

    @BeforeEach
    void setUp() {
        movieId = UUID.randomUUID();
        screenId = UUID.randomUUID();
        theaterId = UUID.randomUUID();
        cityId = UUID.randomUUID();
        vendorId = UUID.randomUUID();

        Users vendor = new Users();
        vendor.setId(vendorId);

        testMovie = new Movie();
        testMovie.setId(movieId);
        testMovie.setTitle("Inception");
        testMovie.setDurationMinutes(148);

        City city = new City("Mumbai", null);
        city.setId(cityId);

        testTheater = new Theater();
        testTheater.setId(theaterId);
        testTheater.setName("PVR Phoenix");
        testTheater.setCity(city);
        testTheater.setVendor(vendor);

        testScreen = new Screen();
        testScreen.setId(screenId);
        testScreen.setName("Screen 1 - IMAX 3D");
        testScreen.setScreenType(ScreenType.IMAX_3D);
        testScreen.setSoundSystem("IMAX 12-Channel");
        testScreen.setTheater(testTheater);
        testScreen.setTotalSeats(100);

        vendorPrincipal = new UserPrincipal(
                vendorId, "Manager", "vendor", "vendor@pvr.com", "hash",
                AccountStatus.ACTIVE, List.of(new SimpleGrantedAuthority("ROLE_THEATER_VENDOR"))
        );
    }

    @Test
    @DisplayName("createShow calculates end time, passes collision check, provisions ShowSeats, and returns ShowResponse")
    void createShow_success() {
        LocalDateTime startTime = LocalDateTime.of(2026, 10, 16, 9, 30);
        CreateShowRequest request = new CreateShowRequest(movieId, screenId, startTime, new BigDecimal("250.00"), 10);

        SeatingTier tier = new SeatingTier(UUID.randomUUID(), "Standard", TierType.STANDARD, BigDecimal.ONE, "Desc", true);
        Seat seat1 = new Seat(testScreen, "A", 1, tier, 0, 0, false, false);
        Seat seat2 = new Seat(testScreen, "A", 2, tier, 0, 1, false, false);

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(testMovie));
        when(screenRepository.findByIdWithTheater(screenId)).thenReturn(Optional.of(testScreen));
        when(showRepository.findCollisions(eq(screenId), any(), any(), isNull())).thenReturn(Collections.emptyList());
        when(seatRepository.findByScreenIdAndActiveTrueOrderByGridRowAscGridColumnAsc(screenId)).thenReturn(List.of(seat1, seat2));

        when(showRepository.save(any(Show.class))).thenAnswer(invocation -> {
            Show s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            s.setCreatedAt(Instant.now());
            s.setUpdatedAt(Instant.now());
            return s;
        });

        ShowResponse response = showService.createShow(request, vendorPrincipal);

        assertNotNull(response);
        assertEquals(movieId, response.getMovieId());
        assertEquals("Inception", response.getMovieTitle());
        assertEquals(screenId, response.getScreenId());
        assertEquals("Screen 1 - IMAX 3D", response.getScreenName());
        assertEquals(new BigDecimal("250.00"), response.getBasePrice());
        // Duration: 148 mins + 10 mins trailers = 158 mins -> 9:30 + 158 mins = 12:08
        assertEquals(startTime.plusMinutes(158), response.getEndTime());
        assertEquals(2, response.getTotalSeats());

        verify(showSeatRepository, times(1)).saveAll(any());
    }

    @Test
    @DisplayName("createShow throws IllegalArgumentException when overlapping show detected within 30-min buffer")
    void createShow_collisionDetected_throwsException() {
        LocalDateTime startTime = LocalDateTime.of(2026, 10, 16, 9, 30);
        CreateShowRequest request = new CreateShowRequest(movieId, screenId, startTime, new BigDecimal("250.00"), 10);

        Show conflictingShow = new Show();
        conflictingShow.setStartTime(LocalDateTime.of(2026, 10, 16, 8, 0));
        conflictingShow.setEndTime(LocalDateTime.of(2026, 10, 16, 10, 0));

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(testMovie));
        when(screenRepository.findByIdWithTheater(screenId)).thenReturn(Optional.of(testScreen));
        when(showRepository.findCollisions(eq(screenId), any(), any(), isNull())).thenReturn(List.of(conflictingShow));

        assertThrows(IllegalArgumentException.class, () -> showService.createShow(request, vendorPrincipal));
        verify(showRepository, never()).save(any());
        verify(showSeatRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("createShow throws AccessDeniedException when caller is not the theater owner")
    void createShow_unauthorized_throwsAccessDeniedException() {
        UserPrincipal otherVendor = new UserPrincipal(
                UUID.randomUUID(), "Other", "other", "other@pvr.com", "hash",
                AccountStatus.ACTIVE, List.of(new SimpleGrantedAuthority("ROLE_THEATER_VENDOR"))
        );

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(testMovie));
        when(screenRepository.findByIdWithTheater(screenId)).thenReturn(Optional.of(testScreen));

        CreateShowRequest request = new CreateShowRequest(movieId, screenId, LocalDateTime.now().plusDays(1), new BigDecimal("200.00"), 10);

        assertThrows(AccessDeniedException.class, () -> showService.createShow(request, otherVendor));
        verify(showRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBatchDailySchedule automatically schedules 5 shows in a day with 30-minute turnaround intervals")
    void createBatchDailySchedule_success() {
        LocalDate showDate = LocalDate.of(2026, 10, 16);
        LocalTime firstShowTime = LocalTime.of(9, 30);

        BatchDailyScheduleRequest batchRequest = new BatchDailyScheduleRequest(
                movieId, screenId, showDate, firstShowTime, 5, 30, 10, new BigDecimal("200.00")
        );

        SeatingTier tier = new SeatingTier(UUID.randomUUID(), "Standard", TierType.STANDARD, BigDecimal.ONE, "Desc", true);
        Seat seat1 = new Seat(testScreen, "A", 1, tier, 0, 0, false, false);

        when(movieRepository.findById(movieId)).thenReturn(Optional.of(testMovie));
        when(screenRepository.findByIdWithTheater(screenId)).thenReturn(Optional.of(testScreen));
        when(showRepository.findCollisions(eq(screenId), any(), any(), isNull())).thenReturn(Collections.emptyList());
        when(seatRepository.findByScreenIdAndActiveTrueOrderByGridRowAscGridColumnAsc(screenId)).thenReturn(List.of(seat1));
        when(showRepository.save(any(Show.class))).thenAnswer(invocation -> {
            Show s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        BatchDailyScheduleResponse response = showService.createBatchDailySchedule(batchRequest, vendorPrincipal);

        assertNotNull(response);
        assertEquals(5, response.getScheduledShowsCount());
        assertEquals(5, response.getShows().size());
        assertEquals("Inception", response.getMovieTitle());
        assertEquals("Screen 1 - IMAX 3D", response.getScreenName());

        // Verify time sequencing:
        // Show 1: 09:30 - 12:08 (158 mins)
        // Show 2: 12:38 - 15:16 (12:08 + 30m turnaround = 12:38)
        ShowResponse show1 = response.getShows().get(0);
        ShowResponse show2 = response.getShows().get(1);

        assertEquals(LocalDateTime.of(2026, 10, 16, 9, 30), show1.getStartTime());
        assertEquals(LocalDateTime.of(2026, 10, 16, 12, 8), show1.getEndTime());
        assertEquals(LocalDateTime.of(2026, 10, 16, 12, 38), show2.getStartTime());

        verify(showRepository, times(5)).save(any(Show.class));
        verify(showSeatRepository, times(5)).saveAll(any());
    }

    @Test
    @DisplayName("getMovieScheduleByCityAndDate returns BookMyShow-style schedules grouped by theater and screen")
    void getMovieScheduleByCityAndDate_returnsGroupedResponse() {
        LocalDate date = LocalDate.of(2026, 10, 16);

        Show show1 = new Show(testMovie, testScreen, date.atTime(9, 30), date.atTime(12, 0), new BigDecimal("200.00"));
        show1.setId(UUID.randomUUID());
        Show show2 = new Show(testMovie, testScreen, date.atTime(12, 30), date.atTime(15, 0), new BigDecimal("250.00"));
        show2.setId(UUID.randomUUID());

        when(movieRepository.existsById(movieId)).thenReturn(true);
        when(cityRepository.existsById(cityId)).thenReturn(true);
        when(showRepository.findShowsByMovieAndCityAndDate(eq(movieId), eq(cityId), any(), any()))
                .thenReturn(List.of(show1, show2));
        when(showSeatRepository.countByShowIdAndStatus(any(), any())).thenReturn(100L);

        List<MovieScheduleGroupResponse> response = showService.getMovieScheduleByCityAndDate(movieId, cityId, date);

        assertNotNull(response);
        assertEquals(1, response.size());
        MovieScheduleGroupResponse theaterGroup = response.get(0);
        assertEquals("PVR Phoenix", theaterGroup.getTheaterName());
        assertEquals(1, theaterGroup.getScreens().size());
        MovieScheduleGroupResponse.ScreenScheduleDto screenGroup = theaterGroup.getScreens().get(0);
        assertEquals("Screen 1 - IMAX 3D", screenGroup.getScreenName());
        assertEquals(2, screenGroup.getShowtimes().size());
    }
}
