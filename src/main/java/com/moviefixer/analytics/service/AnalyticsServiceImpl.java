package com.moviefixer.analytics.service;

import com.moviefixer.analytics.dto.*;
import com.moviefixer.analytics.repository.AnalyticsRepository;
import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.show.entity.Show;
import com.moviefixer.show.enums.ShowSeatStatus;
import com.moviefixer.show.repository.ShowRepository;
import com.moviefixer.show.repository.ShowSeatRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AnalyticsServiceImpl implements AnalyticsService {

    private final AnalyticsRepository analyticsRepository;
    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;

    public AnalyticsServiceImpl(
            AnalyticsRepository analyticsRepository,
            ShowRepository showRepository,
            ShowSeatRepository showSeatRepository) {
        this.analyticsRepository = analyticsRepository;
        this.showRepository = showRepository;
        this.showSeatRepository = showSeatRepository;
    }

    @Override
    public PlatformOverviewResponse getPlatformOverview(UserPrincipal currentUser) {
        validateAdminRole(currentUser);
        return analyticsRepository.getPlatformOverview();
    }

    @Override
    public List<TopMovieAnalyticsResponse> getTopMovies(int limit, UserPrincipal currentUser) {
        validateAdminRole(currentUser);
        int effectiveLimit = (limit > 0) ? limit : 10;
        return analyticsRepository.getTopMovies(effectiveLimit);
    }

    @Override
    public List<CityAnalyticsResponse> getCityAnalytics(UserPrincipal currentUser) {
        validateAdminRole(currentUser);
        return analyticsRepository.getCityAnalytics();
    }

    @Override
    public List<DailyRevenueTrendResponse> getDailyRevenueTrend(int days, UserPrincipal currentUser) {
        validateAdminRole(currentUser);
        int effectiveDays = (days > 0) ? days : 30;
        return analyticsRepository.getDailyRevenueTrend(effectiveDays);
    }

    @Override
    public VendorOverviewResponse getVendorOverview(UserPrincipal currentUser) {
        validateVendorOrAdmin(currentUser);
        return analyticsRepository.getVendorOverview(currentUser.getId(), currentUser.getName());
    }

    @Override
    public List<VendorTheaterAnalyticsResponse> getVendorTheaters(UserPrincipal currentUser) {
        validateVendorOrAdmin(currentUser);
        return analyticsRepository.getVendorTheaters(currentUser.getId());
    }

    @Override
    public List<VendorMovieAnalyticsResponse> getVendorMovies(UserPrincipal currentUser) {
        validateVendorOrAdmin(currentUser);
        return analyticsRepository.getVendorMovies(currentUser.getId());
    }

    @Override
    public ShowOccupancyAnalyticsResponse getShowOccupancy(UUID showId, UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new AccessDeniedException("User must be authenticated to view show occupancy");
        }

        Show show = showRepository.findByIdWithDetails(showId)
                .orElseThrow(() -> new ResourceNotFoundException("Show not found with id: " + showId));

        boolean isAdmin = hasRole(currentUser, "ROLE_ADMIN");
        boolean isVendor = hasRole(currentUser, "ROLE_THEATER_VENDOR");
        boolean isManager = hasRole(currentUser, "ROLE_THEATER_MANAGER");

        if (!isAdmin) {
            if (isVendor) {
                UUID theaterVendorId = (show.getScreen().getTheater().getVendor() != null)
                        ? show.getScreen().getTheater().getVendor().getId()
                        : null;
                if (!currentUser.getId().equals(theaterVendorId)) {
                    throw new AccessDeniedException("You are not authorized to view analytics for this theater");
                }
            } else if (!isManager) {
                throw new AccessDeniedException("Insufficient permissions to view show analytics");
            }
        }

        long totalCapacity = show.getScreen().getTotalSeats() != null ? show.getScreen().getTotalSeats() : 0;
        long bookedSeats = showSeatRepository.countByShowIdAndStatus(showId, ShowSeatStatus.BOOKED);
        long availableSeats = showSeatRepository.countByShowIdAndStatus(showId, ShowSeatStatus.AVAILABLE);
        long lockedSeats = showSeatRepository.countByShowIdAndStatus(showId, ShowSeatStatus.LOCKED);

        double occupancyPercentage = (totalCapacity > 0)
                ? BigDecimal.valueOf((double) bookedSeats / totalCapacity * 100.0)
                .setScale(2, RoundingMode.HALF_UP).doubleValue()
                : 0.0;

        List<TierBreakdownResponse> tierBreakdowns = analyticsRepository.getShowTierBreakdowns(showId);

        BigDecimal grossRevenue = tierBreakdowns.stream()
                .map(TierBreakdownResponse::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ShowOccupancyAnalyticsResponse(
                show.getId(),
                show.getMovie().getTitle(),
                show.getScreen().getTheater().getName(),
                show.getScreen().getName(),
                show.getStartTime(),
                show.getEndTime(),
                show.getStatus().name(),
                totalCapacity,
                bookedSeats,
                availableSeats,
                lockedSeats,
                occupancyPercentage,
                grossRevenue,
                tierBreakdowns
        );
    }

    private void validateAdminRole(UserPrincipal currentUser) {
        if (currentUser == null || !hasRole(currentUser, "ROLE_ADMIN")) {
            throw new AccessDeniedException("Only platform administrators can access system-wide analytics");
        }
    }

    private void validateVendorOrAdmin(UserPrincipal currentUser) {
        if (currentUser == null || (!hasRole(currentUser, "ROLE_THEATER_VENDOR") && !hasRole(currentUser, "ROLE_ADMIN"))) {
            throw new AccessDeniedException("Only theater vendors and administrators can access vendor dashboards");
        }
    }

    private boolean hasRole(UserPrincipal user, String roleName) {
        return user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(roleName));
    }
}
