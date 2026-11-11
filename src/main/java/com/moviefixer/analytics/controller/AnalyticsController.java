package com.moviefixer.analytics.controller;

import com.moviefixer.analytics.dto.*;
import com.moviefixer.analytics.service.AnalyticsService;
import com.moviefixer.auth.security.CurrentUser;
import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/analytics")
@Tag(name = "Analytics & Multi-Vendor Dashboards", description = "Endpoints for executive platform oversight, multi-tenant vendor box office metrics, and show occupancy utilization")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/platform/overview")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Platform executive metrics", description = "Calculates platform-wide Gross Merchandise Value (GMV), convenience fee revenue, vendor payouts, and booking statistics")
    public ResponseEntity<ApiResponse<PlatformOverviewResponse>> getPlatformOverview(
            @CurrentUser UserPrincipal currentUser) {

        PlatformOverviewResponse response = analyticsService.getPlatformOverview(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Platform executive metrics retrieved successfully", response));
    }

    @GetMapping("/platform/top-movies")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Top grossing movies", description = "Returns leaderboard of top grossing movies across the platform ordered by ticket sales and gross revenue")
    public ResponseEntity<ApiResponse<List<TopMovieAnalyticsResponse>>> getTopMovies(
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @CurrentUser UserPrincipal currentUser) {

        List<TopMovieAnalyticsResponse> response = analyticsService.getTopMovies(limit, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Top grossing movies retrieved successfully", response));
    }

    @GetMapping("/platform/cities")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Regional & city box office performance", description = "Aggregates theater counts, screen counts, ticket volume, and revenue by city")
    public ResponseEntity<ApiResponse<List<CityAnalyticsResponse>>> getCityAnalytics(
            @CurrentUser UserPrincipal currentUser) {

        List<CityAnalyticsResponse> response = analyticsService.getCityAnalytics(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Regional city performance retrieved successfully", response));
    }

    @GetMapping("/platform/revenue-trend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Platform daily revenue trend", description = "Time-series daily breakdown of bookings, tickets sold, and revenue for the last N days")
    public ResponseEntity<ApiResponse<List<DailyRevenueTrendResponse>>> getDailyRevenueTrend(
            @RequestParam(value = "days", defaultValue = "30") int days,
            @CurrentUser UserPrincipal currentUser) {

        List<DailyRevenueTrendResponse> response = analyticsService.getDailyRevenueTrend(days, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Daily revenue trend retrieved successfully", response));
    }

    @GetMapping("/vendor/overview")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_VENDOR')")
    @Operation(summary = "Vendor box office overview", description = "Vendor-scoped dashboard returning gross box office, platform commission deducted, net payout, and gate admission rates")
    public ResponseEntity<ApiResponse<VendorOverviewResponse>> getVendorOverview(
            @CurrentUser UserPrincipal currentUser) {

        VendorOverviewResponse response = analyticsService.getVendorOverview(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Vendor box office overview retrieved successfully", response));
    }

    @GetMapping("/vendor/theaters")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_VENDOR')")
    @Operation(summary = "Vendor individual theater performance", description = "Per-theater breakdown of screens, shows, tickets sold, and revenue for theaters owned by the authenticated vendor")
    public ResponseEntity<ApiResponse<List<VendorTheaterAnalyticsResponse>>> getVendorTheaters(
            @CurrentUser UserPrincipal currentUser) {

        List<VendorTheaterAnalyticsResponse> response = analyticsService.getVendorTheaters(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Vendor theater breakdown retrieved successfully", response));
    }

    @GetMapping("/vendor/movies")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_VENDOR')")
    @Operation(summary = "Vendor movie revenue breakdown", description = "Ranks movies showing across the vendor's theaters by ticket sales and box office revenue")
    public ResponseEntity<ApiResponse<List<VendorMovieAnalyticsResponse>>> getVendorMovies(
            @CurrentUser UserPrincipal currentUser) {

        List<VendorMovieAnalyticsResponse> response = analyticsService.getVendorMovies(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Vendor movie analytics retrieved successfully", response));
    }

    @GetMapping("/shows/{showId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'THEATER_VENDOR', 'THEATER_MANAGER')")
    @Operation(summary = "Show capacity & seat utilization analytics", description = "Returns real-time capacity utilization, booked vs available seats, occupancy percentage, and tier-wise yield")
    public ResponseEntity<ApiResponse<ShowOccupancyAnalyticsResponse>> getShowOccupancy(
            @PathVariable("showId") UUID showId,
            @CurrentUser UserPrincipal currentUser) {

        ShowOccupancyAnalyticsResponse response = analyticsService.getShowOccupancy(showId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Show occupancy analytics retrieved successfully", response));
    }
}
