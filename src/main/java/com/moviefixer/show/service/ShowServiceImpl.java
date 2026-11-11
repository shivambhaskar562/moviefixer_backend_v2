package com.moviefixer.show.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.location.repository.CityRepository;
import com.moviefixer.movie.entity.Movie;
import com.moviefixer.movie.repository.MovieRepository;
import com.moviefixer.show.dto.*;
import com.moviefixer.show.entity.Show;
import com.moviefixer.show.entity.ShowSeat;
import com.moviefixer.show.enums.ShowSeatStatus;
import com.moviefixer.show.enums.ShowStatus;
import com.moviefixer.show.repository.ShowRepository;
import com.moviefixer.show.repository.ShowSeatRepository;
import com.moviefixer.theater.entity.Screen;
import com.moviefixer.theater.entity.Seat;
import com.moviefixer.theater.entity.Theater;
import com.moviefixer.theater.repository.ScreenRepository;
import com.moviefixer.theater.repository.SeatRepository;
import com.moviefixer.theater.repository.TheaterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShowServiceImpl implements ShowService {

    private static final Logger log = LoggerFactory.getLogger(ShowServiceImpl.class);

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final CityRepository cityRepository;
    private final TheaterRepository theaterRepository;

    public ShowServiceImpl(
            ShowRepository showRepository,
            ShowSeatRepository showSeatRepository,
            MovieRepository movieRepository,
            ScreenRepository screenRepository,
            SeatRepository seatRepository,
            CityRepository cityRepository,
            TheaterRepository theaterRepository
    ) {
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.cityRepository = cityRepository;
        this.theaterRepository = theaterRepository;
    }

    @Override
    @Transactional
    public ShowResponse createShow(CreateShowRequest request, UserPrincipal currentUser) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", request.getMovieId()));

        Screen screen = screenRepository.findByIdWithTheater(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", request.getScreenId()));

        validateOwnership(screen.getTheater(), currentUser);

        int trailerMinutes = (request.getTrailerMinutes() != null) ? request.getTrailerMinutes() : 10;
        int totalRuntimeMinutes = movie.getDurationMinutes() + trailerMinutes;

        LocalDateTime startTime = request.getStartTime();
        LocalDateTime endTime = startTime.plusMinutes(totalRuntimeMinutes);

        // Turnaround collision check (30 minutes buffer)
        validateNoCollisions(screen.getId(), screen.getName(), startTime, endTime, 30, null);

        Show show = new Show(movie, screen, startTime, endTime, request.getBasePrice());
        Show savedShow = showRepository.save(show);

        // Provision ShowSeat inventory snapshot
        long seatCount = provisionShowSeatInventory(savedShow, screen, request.getBasePrice());

        log.info("Scheduled show ID: {} for movie '{}' on screen '{}' from {} to {} ({} seats)",
                savedShow.getId(), movie.getTitle(), screen.getName(), startTime, endTime, seatCount);

        return mapToShowResponse(savedShow, seatCount, seatCount);
    }

    @Override
    @Transactional
    public BatchDailyScheduleResponse createBatchDailySchedule(BatchDailyScheduleRequest request, UserPrincipal currentUser) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", request.getMovieId()));

        Screen screen = screenRepository.findByIdWithTheater(request.getScreenId())
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", request.getScreenId()));

        validateOwnership(screen.getTheater(), currentUser);

        int trailerMinutes = (request.getTrailerMinutes() != null) ? request.getTrailerMinutes() : 10;
        int turnaroundMinutes = (request.getTurnaroundMinutes() != null) ? request.getTurnaroundMinutes() : 30;
        int totalSlotDuration = movie.getDurationMinutes() + trailerMinutes;

        LocalDateTime currentSlotStart = request.getDate().atTime(request.getFirstShowTime());
        List<ShowResponse> generatedShows = new ArrayList<>();

        for (int i = 0; i < request.getNumberOfShows(); i++) {
            LocalDateTime currentSlotEnd = currentSlotStart.plusMinutes(totalSlotDuration);

            // Validate collision with existing shows outside this batch
            validateNoCollisions(screen.getId(), screen.getName(), currentSlotStart, currentSlotEnd, turnaroundMinutes, null);

            Show show = new Show(movie, screen, currentSlotStart, currentSlotEnd, request.getBasePrice());
            Show savedShow = showRepository.save(show);

            long seatCount = provisionShowSeatInventory(savedShow, screen, request.getBasePrice());
            generatedShows.add(mapToShowResponse(savedShow, seatCount, seatCount));

            // Calculate next showtime start = current end + turnaround cleaning buffer
            currentSlotStart = currentSlotEnd.plusMinutes(turnaroundMinutes);
        }

        log.info("Batch scheduled {} shows of movie '{}' on screen '{}' for date {}",
                generatedShows.size(), movie.getTitle(), screen.getName(), request.getDate());

        return new BatchDailyScheduleResponse(
                movie.getId(),
                movie.getTitle(),
                screen.getId(),
                screen.getName(),
                screen.getTheater().getName(),
                request.getDate(),
                generatedShows.size(),
                generatedShows
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ShowResponse getShowById(UUID showId) {
        Show show = showRepository.findByIdWithDetails(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", showId));

        long totalSeats = showSeatRepository.countByShowId(showId);
        long availableSeats = showSeatRepository.countByShowIdAndStatus(showId, ShowSeatStatus.AVAILABLE);

        return mapToShowResponse(show, totalSeats, availableSeats);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieScheduleGroupResponse> getMovieScheduleByCityAndDate(UUID movieId, UUID cityId, LocalDate date) {
        if (!movieRepository.existsById(movieId)) {
            throw new ResourceNotFoundException("Movie", "id", movieId);
        }
        if (!cityRepository.existsById(cityId)) {
            throw new ResourceNotFoundException("City", "id", cityId);
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Show> shows = showRepository.findShowsByMovieAndCityAndDate(movieId, cityId, startOfDay, endOfDay);

        // Group by Theater -> Group by Screen -> Map to time pills
        Map<Theater, Map<Screen, List<Show>>> grouped = shows.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getScreen().getTheater(),
                        LinkedHashMap::new,
                        Collectors.groupingBy(Show::getScreen, LinkedHashMap::new, Collectors.toList())
                ));

        List<MovieScheduleGroupResponse> result = new ArrayList<>();
        for (Map.Entry<Theater, Map<Screen, List<Show>>> theaterEntry : grouped.entrySet()) {
            Theater theater = theaterEntry.getKey();

            List<MovieScheduleGroupResponse.ScreenScheduleDto> screenDtos = new ArrayList<>();
            for (Map.Entry<Screen, List<Show>> screenEntry : theaterEntry.getValue().entrySet()) {
                Screen screen = screenEntry.getKey();
                List<ShowSummaryResponse> showtimes = screenEntry.getValue().stream()
                        .map(s -> {
                            long available = showSeatRepository.countByShowIdAndStatus(s.getId(), ShowSeatStatus.AVAILABLE);
                            return new ShowSummaryResponse(
                                    s.getId(),
                                    s.getScreen().getId(),
                                    s.getScreen().getName(),
                                    s.getScreen().getScreenType(),
                                    s.getStartTime(),
                                    s.getEndTime(),
                                    s.getBasePrice(),
                                    s.getStatus(),
                                    available
                            );
                        })
                        .toList();

                screenDtos.add(new MovieScheduleGroupResponse.ScreenScheduleDto(
                        screen.getId(),
                        screen.getName(),
                        screen.getScreenType().name(),
                        screen.getSoundSystem(),
                        showtimes
                ));
            }

            result.add(new MovieScheduleGroupResponse(
                    theater.getId(),
                    theater.getName(),
                    theater.getAddress(),
                    theater.getCity().getName(),
                    screenDtos
            ));
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShowSummaryResponse> getShowsByTheaterAndDate(UUID theaterId, LocalDate date) {
        if (!theaterRepository.existsById(theaterId)) {
            throw new ResourceNotFoundException("Theater", "id", theaterId);
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return showRepository.findShowsByTheaterAndDate(theaterId, startOfDay, endOfDay)
                .stream()
                .map(s -> {
                    long available = showSeatRepository.countByShowIdAndStatus(s.getId(), ShowSeatStatus.AVAILABLE);
                    return new ShowSummaryResponse(
                            s.getId(),
                            s.getScreen().getId(),
                            s.getScreen().getName(),
                            s.getScreen().getScreenType(),
                            s.getStartTime(),
                            s.getEndTime(),
                            s.getBasePrice(),
                            s.getStatus(),
                            available
                    );
                })
                .toList();
    }

    @Override
    @Transactional
    public void cancelShow(UUID showId, UserPrincipal currentUser) {
        Show show = showRepository.findByIdWithDetails(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", showId));

        validateOwnership(show.getScreen().getTheater(), currentUser);

        show.setStatus(ShowStatus.CANCELLED);
        showRepository.save(show);
        log.info("Cancelled show ID: {} for movie '{}'", show.getId(), show.getMovie().getTitle());
    }

    private void validateNoCollisions(UUID screenId, String screenName, LocalDateTime startTime,
                                      LocalDateTime endTime, int bufferMinutes, UUID excludeShowId) {
        LocalDateTime startWithBuffer = startTime.minusMinutes(bufferMinutes);
        LocalDateTime endWithBuffer = endTime.plusMinutes(bufferMinutes);

        List<Show> collisions = showRepository.findCollisions(screenId, startWithBuffer, endWithBuffer, excludeShowId);
        if (!collisions.isEmpty()) {
            Show firstConflict = collisions.get(0);
            throw new IllegalArgumentException(
                    "Schedule collision on Screen '" + screenName + "': Conflicting show exists from " +
                            firstConflict.getStartTime() + " to " + firstConflict.getEndTime() +
                            " (requiring " + bufferMinutes + "m turnaround buffer)"
            );
        }
    }

    private long provisionShowSeatInventory(Show show, Screen screen, BigDecimal basePrice) {
        List<Seat> physicalSeats = seatRepository.findByScreenIdAndActiveTrueOrderByGridRowAscGridColumnAsc(screen.getId());
        if (physicalSeats.isEmpty()) {
            throw new IllegalStateException("Cannot schedule show on Screen '" + screen.getName() + "' because it has no configured seating layout.");
        }

        List<ShowSeat> showSeats = new ArrayList<>();
        for (Seat seat : physicalSeats) {
            BigDecimal multiplier = (seat.getTier() != null) ? seat.getTier().getDefaultMultiplier() : BigDecimal.ONE;
            BigDecimal calculatedPrice = basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);

            ShowSeat showSeat = new ShowSeat(show, seat, calculatedPrice);
            showSeats.add(showSeat);
        }

        showSeatRepository.saveAll(showSeats);
        return showSeats.size();
    }

    private void validateOwnership(Theater theater, UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !theater.getVendor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to manage shows for theater " + theater.getName());
        }
    }

    private ShowResponse mapToShowResponse(Show show, long totalSeats, long availableSeats) {
        Screen screen = show.getScreen();
        Theater theater = (screen != null) ? screen.getTheater() : null;
        Movie movie = show.getMovie();

        return new ShowResponse(
                show.getId(),
                (movie != null) ? movie.getId() : null,
                (movie != null) ? movie.getTitle() : null,
                (movie != null) ? movie.getPosterUrl() : null,
                (movie != null) ? movie.getDurationMinutes() : null,
                (theater != null) ? theater.getId() : null,
                (theater != null) ? theater.getName() : null,
                (theater != null) ? theater.getAddress() : null,
                (screen != null) ? screen.getId() : null,
                (screen != null) ? screen.getName() : null,
                (screen != null) ? screen.getScreenType() : null,
                (screen != null) ? screen.getSoundSystem() : null,
                show.getStartTime(),
                show.getEndTime(),
                show.getBasePrice(),
                show.getStatus(),
                totalSeats,
                availableSeats,
                show.getCreatedAt(),
                show.getUpdatedAt()
        );
    }
}
