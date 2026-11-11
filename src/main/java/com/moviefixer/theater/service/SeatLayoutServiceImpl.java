package com.moviefixer.theater.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.theater.dto.*;
import com.moviefixer.theater.entity.Screen;
import com.moviefixer.theater.entity.Seat;
import com.moviefixer.theater.entity.SeatingTier;
import com.moviefixer.theater.entity.Theater;
import com.moviefixer.theater.repository.ScreenRepository;
import com.moviefixer.theater.repository.SeatRepository;
import com.moviefixer.theater.repository.SeatingTierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatLayoutServiceImpl implements SeatLayoutService {

    private static final Logger log = LoggerFactory.getLogger(SeatLayoutServiceImpl.class);

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;
    private final SeatingTierRepository seatingTierRepository;

    public SeatLayoutServiceImpl(
            SeatRepository seatRepository,
            ScreenRepository screenRepository,
            SeatingTierRepository seatingTierRepository
    ) {
        this.seatRepository = seatRepository;
        this.screenRepository = screenRepository;
        this.seatingTierRepository = seatingTierRepository;
    }

    @Override
    @Transactional
    public ScreenLayoutResponse configureBatchLayout(UUID screenId, BatchSeatLayoutRequest request, UserPrincipal currentUser) {
        Screen screen = screenRepository.findByIdWithTheater(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", screenId));

        validateOwnership(screen.getTheater(), currentUser);

        if (request.getSeats() == null || request.getSeats().isEmpty()) {
            throw new IllegalArgumentException("Seat layout list cannot be empty");
        }

        // 1. Resolve and validate all seating tiers
        Set<UUID> requestedTierIds = request.getSeats().stream()
                .map(SeatItemDto::getTierId)
                .collect(Collectors.toSet());

        List<SeatingTier> tiers = seatingTierRepository.findAllById(requestedTierIds);
        Map<UUID, SeatingTier> tierMap = tiers.stream()
                .collect(Collectors.toMap(SeatingTier::getId, t -> t));

        for (UUID tierId : requestedTierIds) {
            if (!tierMap.containsKey(tierId)) {
                throw new ResourceNotFoundException("SeatingTier", "id", tierId);
            }
        }

        // 2. Validate coordinates & unique seat numbers
        Set<String> seatNumberKeys = new HashSet<>();
        Set<String> gridCoordKeys = new HashSet<>();

        List<Seat> newSeats = new ArrayList<>();
        int activeCount = 0;

        for (SeatItemDto item : request.getSeats()) {
            String seatKey = item.getRowIdentifier().trim().toUpperCase(Locale.ROOT) + ":" + item.getSeatNumber();
            if (!seatNumberKeys.add(seatKey)) {
                throw new IllegalArgumentException("Duplicate seat row/number in request: " + seatKey);
            }

            String gridKey = item.getGridRow() + ":" + item.getGridColumn();
            if (!gridCoordKeys.add(gridKey)) {
                throw new IllegalArgumentException("Duplicate grid coordinate in request: [row=" + item.getGridRow() + ", col=" + item.getGridColumn() + "]");
            }

            boolean isActive = (item.getActive() != null) ? item.getActive() : true;
            if (isActive) {
                activeCount++;
            }

            Seat seat = new Seat(
                    screen,
                    item.getRowIdentifier().trim().toUpperCase(Locale.ROOT),
                    item.getSeatNumber(),
                    tierMap.get(item.getTierId()),
                    item.getGridRow(),
                    item.getGridColumn(),
                    (item.getIsAisle() != null) ? item.getIsAisle() : false,
                    (item.getIsWheelchairAccessible() != null) ? item.getIsWheelchairAccessible() : false
            );
            seat.setActive(isActive);
            newSeats.add(seat);
        }

        // 3. Delete existing layout and save new batch
        seatRepository.deleteByScreenId(screenId);
        List<Seat> savedSeats = seatRepository.saveAll(newSeats);

        // 4. Update total seats on screen
        screen.setTotalSeats(activeCount);
        screenRepository.save(screen);

        log.info("Configured custom layout for Screen ID: {} with {} total active seats", screenId, activeCount);
        return buildScreenLayoutResponse(screen, savedSeats);
    }

    @Override
    @Transactional
    public ScreenLayoutResponse generateGridLayout(UUID screenId, GenerateGridRequest request, UserPrincipal currentUser) {
        Screen screen = screenRepository.findByIdWithTheater(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", screenId));

        validateOwnership(screen.getTheater(), currentUser);

        SeatingTier defaultTier = seatingTierRepository.findById(request.getDefaultTierId())
                .orElseThrow(() -> new ResourceNotFoundException("SeatingTier", "id", request.getDefaultTierId()));

        Map<String, SeatingTier> overrideMap = new HashMap<>();
        if (request.getRowTierOverrides() != null && !request.getRowTierOverrides().isEmpty()) {
            for (Map.Entry<String, UUID> entry : request.getRowTierOverrides().entrySet()) {
                SeatingTier tier = seatingTierRepository.findById(entry.getValue())
                        .orElseThrow(() -> new ResourceNotFoundException("SeatingTier", "id", entry.getValue()));
                overrideMap.put(entry.getKey().toUpperCase(Locale.ROOT), tier);
            }
        }

        Set<Integer> aisleCols = (request.getAisleColumns() != null)
                ? new HashSet<>(request.getAisleColumns())
                : Collections.emptySet();

        Set<String> wheelchairIdentifiers = (request.getWheelchairSeats() != null)
                ? request.getWheelchairSeats().stream().map(s -> s.toUpperCase(Locale.ROOT)).collect(Collectors.toSet())
                : Collections.emptySet();

        List<Seat> generatedSeats = new ArrayList<>();
        int totalRows = request.getTotalRows();
        int seatsPerRow = request.getSeatsPerRow();

        for (int r = 0; r < totalRows; r++) {
            String rowLetter = String.valueOf((char) ('A' + r));
            SeatingTier rowTier = overrideMap.getOrDefault(rowLetter, defaultTier);

            for (int c = 1; c <= seatsPerRow; c++) {
                boolean isAisle = aisleCols.contains(c);
                boolean isWheelchair = wheelchairIdentifiers.contains(rowLetter + c);

                Seat seat = new Seat(
                        screen,
                        rowLetter,
                        c,
                        rowTier,
                        r, // 0-indexed grid row
                        c - 1, // 0-indexed grid col
                        isAisle,
                        isWheelchair
                );
                generatedSeats.add(seat);
            }
        }

        // Delete existing and persist new
        seatRepository.deleteByScreenId(screenId);
        List<Seat> savedSeats = seatRepository.saveAll(generatedSeats);

        screen.setTotalSeats(savedSeats.size());
        screenRepository.save(screen);

        log.info("Auto-generated {}x{} grid for Screen ID: {} ({} seats total)", totalRows, seatsPerRow, screenId, savedSeats.size());
        return buildScreenLayoutResponse(screen, savedSeats);
    }

    @Override
    @Transactional(readOnly = true)
    public ScreenLayoutResponse getScreenLayout(UUID screenId) {
        Screen screen = screenRepository.findByIdWithTheater(screenId)
                .orElseThrow(() -> new ResourceNotFoundException("Screen", "id", screenId));

        List<Seat> seats = seatRepository.findByScreenIdOrderByGridRowAscGridColumnAsc(screenId);
        return buildScreenLayoutResponse(screen, seats);
    }

    @Override
    @Transactional
    public SeatResponse toggleSeatStatus(UUID seatId, boolean active, UserPrincipal currentUser) {
        Seat seat = seatRepository.findByIdWithDetails(seatId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", seatId));

        validateOwnership(seat.getScreen().getTheater(), currentUser);

        seat.setActive(active);
        Seat updated = seatRepository.save(seat);

        // Recalculate screen seat capacity
        long activeCount = seatRepository.countByScreenIdAndActiveTrue(seat.getScreen().getId());
        Screen screen = seat.getScreen();
        screen.setTotalSeats((int) activeCount);
        screenRepository.save(screen);

        log.info("Seat {} toggled to active={}", seat.getRowIdentifier() + seat.getSeatNumber(), active);
        return mapToSeatResponse(updated);
    }

    private void validateOwnership(Theater theater, UserPrincipal currentUser) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !theater.getVendor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to manage layout for theater " + theater.getName());
        }
    }

    private ScreenLayoutResponse buildScreenLayoutResponse(Screen screen, List<Seat> seats) {
        int maxRow = 0;
        int maxCol = 0;
        Map<UUID, SeatingTierResponse> distinctTiers = new LinkedHashMap<>();

        List<SeatResponse> seatResponses = new ArrayList<>();
        for (Seat seat : seats) {
            if (seat.getGridRow() > maxRow) {
                maxRow = seat.getGridRow();
            }
            if (seat.getGridColumn() > maxCol) {
                maxCol = seat.getGridColumn();
            }

            SeatingTier tier = seat.getTier();
            if (tier != null && !distinctTiers.containsKey(tier.getId())) {
                distinctTiers.put(tier.getId(), new SeatingTierResponse(
                        tier.getId(),
                        tier.getName(),
                        tier.getTierType(),
                        tier.getDefaultMultiplier(),
                        tier.getDescription(),
                        tier.isActive()
                ));
            }

            seatResponses.add(mapToSeatResponse(seat));
        }

        Theater theater = screen.getTheater();
        return new ScreenLayoutResponse(
                screen.getId(),
                screen.getName(),
                (theater != null) ? theater.getId() : null,
                (theater != null) ? theater.getName() : null,
                screen.getTotalSeats(),
                seats.isEmpty() ? 0 : maxRow + 1,
                seats.isEmpty() ? 0 : maxCol + 1,
                new ArrayList<>(distinctTiers.values()),
                seatResponses
        );
    }

    private SeatResponse mapToSeatResponse(Seat seat) {
        SeatingTier tier = seat.getTier();
        return new SeatResponse(
                seat.getId(),
                seat.getScreen().getId(),
                seat.getRowIdentifier(),
                seat.getSeatNumber(),
                (tier != null) ? tier.getId() : null,
                (tier != null) ? tier.getName() : null,
                (tier != null) ? tier.getTierType() : null,
                (tier != null) ? tier.getDefaultMultiplier() : null,
                seat.getGridRow(),
                seat.getGridColumn(),
                seat.isAisle(),
                seat.isWheelchairAccessible(),
                seat.isActive()
        );
    }
}
