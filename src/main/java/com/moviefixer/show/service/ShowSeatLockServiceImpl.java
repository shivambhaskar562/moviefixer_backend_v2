package com.moviefixer.show.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.show.dto.LockSeatsRequest;
import com.moviefixer.show.dto.LockSeatsResponse;
import com.moviefixer.show.dto.ShowSeatLayoutResponse;
import com.moviefixer.show.dto.ShowSeatResponse;
import com.moviefixer.show.entity.Show;
import com.moviefixer.show.entity.ShowSeat;
import com.moviefixer.show.enums.ShowSeatStatus;
import com.moviefixer.show.repository.ShowRepository;
import com.moviefixer.show.repository.ShowSeatRepository;
import com.moviefixer.theater.entity.Screen;
import com.moviefixer.theater.entity.Seat;
import com.moviefixer.theater.entity.SeatingTier;
import com.moviefixer.theater.entity.Theater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ShowSeatLockServiceImpl implements ShowSeatLockService {

    private static final Logger log = LoggerFactory.getLogger(ShowSeatLockServiceImpl.class);

    private static final int LOCK_DURATION_MINUTES = 10;

    private final ShowSeatRepository showSeatRepository;
    private final ShowRepository showRepository;

    public ShowSeatLockServiceImpl(ShowSeatRepository showSeatRepository, ShowRepository showRepository) {
        this.showSeatRepository = showSeatRepository;
        this.showRepository = showRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ShowSeatLayoutResponse getShowSeatLayout(UUID showId, UserPrincipal currentUser) {
        Show show = showRepository.findByIdWithDetails(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show", "id", showId));

        List<ShowSeat> showSeats = showSeatRepository.findByShowIdWithSeatDetails(showId);
        long availableCount = 0;

        List<ShowSeatResponse> seatResponses = new ArrayList<>();
        UUID currentUserId = (currentUser != null) ? currentUser.getId() : null;
        LocalDateTime now = LocalDateTime.now();

        for (ShowSeat ss : showSeats) {
            ShowSeatStatus effectiveStatus = ss.getStatus();
            // If lock timer has elapsed, treat as AVAILABLE in layout view
            if (effectiveStatus == ShowSeatStatus.LOCKED && ss.getLockedUntil() != null && ss.getLockedUntil().isBefore(now)) {
                effectiveStatus = ShowSeatStatus.AVAILABLE;
            }

            if (effectiveStatus == ShowSeatStatus.AVAILABLE) {
                availableCount++;
            }

            boolean isLockedByMe = (effectiveStatus == ShowSeatStatus.LOCKED)
                    && currentUserId != null
                    && currentUserId.equals(ss.getLockedByUserId());

            seatResponses.add(mapToShowSeatResponse(ss, effectiveStatus, isLockedByMe));
        }

        Screen screen = show.getScreen();
        Theater theater = (screen != null) ? screen.getTheater() : null;

        return new ShowSeatLayoutResponse(
                show.getId(),
                show.getMovie().getId(),
                show.getMovie().getTitle(),
                (theater != null) ? theater.getId() : null,
                (theater != null) ? theater.getName() : null,
                (screen != null) ? screen.getId() : null,
                (screen != null) ? screen.getName() : null,
                (screen != null) ? screen.getScreenType() : null,
                show.getStartTime(),
                show.getEndTime(),
                show.getBasePrice(),
                showSeats.size(),
                availableCount,
                seatResponses
        );
    }

    /**
     * Non-blocking Optimistic Concurrency Control (OCC) using atomic query versioning.
     * Zero DB row-level lock blocking. Fast CAS (Compare-And-Swap) execution.
     */
    @Override
    @Transactional
    public LockSeatsResponse lockSeats(UUID showId, LockSeatsRequest request, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User must be authenticated to lock seats");
        }

        if (request.getSeatIds() == null || request.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("No seats selected for locking");
        }

        if (request.getSeatIds().size() > 10) {
            throw new IllegalArgumentException("Cannot lock more than 10 seats in a single reservation");
        }

        if (!showRepository.existsById(showId)) {
            throw new ResourceNotFoundException("Show", "id", showId);
        }

        // Fast non-blocking read without pessimistic locks
        List<ShowSeat> selectedSeats = showSeatRepository.findByShowIdAndSeatIdIn(showId, request.getSeatIds());

        if (selectedSeats.size() != request.getSeatIds().size()) {
            throw new ResourceNotFoundException("One or more selected seats were not found for this show");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lockedUntil = now.plusMinutes(LOCK_DURATION_MINUTES);
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<ShowSeatResponse> lockedSeatResponses = new ArrayList<>();

        // Execute non-blocking atomic compare-and-swap (CAS) updates checking query version
        for (ShowSeat ss : selectedSeats) {
            Seat seat = ss.getSeat();
            String seatLabel = seat.getRowIdentifier() + seat.getSeatNumber();

            if (ss.getStatus() == ShowSeatStatus.BOOKED) {
                throw new IllegalStateException("Seat " + seatLabel + " is already booked and cannot be reserved");
            }

            if (ss.getStatus() == ShowSeatStatus.LOCKED) {
                if (ss.getLockedUntil() != null && ss.getLockedUntil().isAfter(now)
                        && !currentUser.getId().equals(ss.getLockedByUserId())) {
                    throw new IllegalStateException("Seat " + seatLabel + " is currently locked by another customer");
                }
            }

            // Atomic Query Versioning CAS: Updates only if version matches the fetched version
            int rowsUpdated = showSeatRepository.atomicLockSeatWithVersion(
                    ss.getId(),
                    ss.getVersion(),
                    currentUser.getId(),
                    lockedUntil,
                    now
            );

            if (rowsUpdated == 0) {
                // Version changed concurrently by another transaction!
                throw new IllegalStateException("Seat " + seatLabel + " was just booked or locked by another customer (version conflict). Please select another seat.");
            }

            // Update in-memory state
            ss.setStatus(ShowSeatStatus.LOCKED);
            ss.setLockedByUserId(currentUser.getId());
            ss.setLockedUntil(lockedUntil);
            ss.setVersion(ss.getVersion() + 1);

            totalPrice = totalPrice.add(ss.getPrice());
            lockedSeatResponses.add(mapToShowSeatResponse(ss, ShowSeatStatus.LOCKED, true));
        }

        long expiresInSeconds = Duration.between(now, lockedUntil).getSeconds();
        log.info("Optimistic OCC: User {} locked {} seats for show ID: {} with query versioning",
                currentUser.getUsername(), selectedSeats.size(), showId);

        return new LockSeatsResponse(
                showId,
                request.getSeatIds(),
                lockedUntil,
                expiresInSeconds,
                totalPrice,
                lockedSeatResponses
        );
    }

    @Override
    @Transactional
    public void unlockSeats(UUID showId, List<UUID> seatIds, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new IllegalArgumentException("User must be authenticated to release seats");
        }

        List<ShowSeat> seats = showSeatRepository.findByShowIdAndSeatIdIn(showId, seatIds);
        for (ShowSeat ss : seats) {
            if (ss.getStatus() == ShowSeatStatus.LOCKED && currentUser.getId().equals(ss.getLockedByUserId())) {
                ss.setStatus(ShowSeatStatus.AVAILABLE);
                ss.setLockedByUserId(null);
                ss.setLockedUntil(null);
            }
        }

        showSeatRepository.saveAll(seats);
        log.info("User {} released {} seats for show ID: {}", currentUser.getUsername(), seats.size(), showId);
    }

    @Override
    @Transactional
    public int releaseExpiredLocks() {
        LocalDateTime now = LocalDateTime.now();
        int releasedCount = showSeatRepository.releaseExpiredLocks(now);
        if (releasedCount > 0) {
            log.info("Background lock cleaner: Released {} expired seat reservations", releasedCount);
        }
        return releasedCount;
    }

    private ShowSeatResponse mapToShowSeatResponse(ShowSeat ss, ShowSeatStatus status, boolean isLockedByMe) {
        Seat seat = ss.getSeat();
        SeatingTier tier = (seat != null) ? seat.getTier() : null;

        return new ShowSeatResponse(
                ss.getId(),
                (seat != null) ? seat.getId() : null,
                (seat != null) ? seat.getRowIdentifier() : null,
                (seat != null) ? seat.getSeatNumber() : null,
                (tier != null) ? tier.getId() : null,
                (tier != null) ? tier.getName() : null,
                (tier != null) ? tier.getTierType() : null,
                ss.getPrice(),
                status,
                (seat != null) ? seat.getGridRow() : null,
                (seat != null) ? seat.getGridColumn() : null,
                seat != null && seat.isAisle(),
                seat != null && seat.isWheelchairAccessible(),
                isLockedByMe,
                ss.getVersion()
        );
    }
}
