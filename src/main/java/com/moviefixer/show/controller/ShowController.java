package com.moviefixer.show.controller;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.common.dto.ApiResponse;
import com.moviefixer.show.dto.*;
import com.moviefixer.show.service.ShowSeatLockService;
import com.moviefixer.show.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shows")
@Tag(name = "Shows & Scheduling", description = "Endpoints for screening schedules, multi-slot daily scheduling, and real-time seat locking")
public class ShowController {

    private final ShowService showService;
    private final ShowSeatLockService showSeatLockService;

    public ShowController(ShowService showService, ShowSeatLockService showSeatLockService) {
        this.showService = showService;
        this.showSeatLockService = showSeatLockService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get show details by ID (Public)")
    public ResponseEntity<ApiResponse<ShowResponse>> getShowById(@PathVariable UUID id) {
        ShowResponse response = showService.getShowById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/movie/{movieId}")
    @Operation(summary = "Get BookMyShow-style schedule for a movie grouped by theater and screening format (Public)")
    public ResponseEntity<ApiResponse<List<MovieScheduleGroupResponse>>> getMovieSchedule(
            @PathVariable UUID movieId,
            @RequestParam UUID cityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        List<MovieScheduleGroupResponse> response = showService.getMovieScheduleByCityAndDate(movieId, cityId, queryDate);
        return ResponseEntity.ok(ApiResponse.success("Movie schedule retrieved successfully", response));
    }

    @GetMapping("/theater/{theaterId}")
    @Operation(summary = "Get all shows scheduled in a theater for a specific date (Public)")
    public ResponseEntity<ApiResponse<List<ShowSummaryResponse>>> getShowsByTheater(
            @PathVariable UUID theaterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate queryDate = (date != null) ? date : LocalDate.now();
        List<ShowSummaryResponse> response = showService.getShowsByTheaterAndDate(theaterId, queryDate);
        return ResponseEntity.ok(ApiResponse.success("Theater shows retrieved successfully", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Schedule a single show (Vendor / Admin)")
    public ResponseEntity<ApiResponse<ShowResponse>> createShow(
            @Valid @RequestBody CreateShowRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ShowResponse response = showService.createShow(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Show scheduled successfully", response));
    }

    @PostMapping("/batch-daily")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Auto-schedule 5 to 6 shows in a day with turnaround buffer (Vendor / Admin)")
    public ResponseEntity<ApiResponse<BatchDailyScheduleResponse>> createBatchDailySchedule(
            @Valid @RequestBody BatchDailyScheduleRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        BatchDailyScheduleResponse response = showService.createBatchDailySchedule(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Daily shows batch scheduled successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Cancel a show (Vendor owner or Admin)")
    public ResponseEntity<ApiResponse<Void>> cancelShow(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        showService.cancelShow(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Show cancelled successfully", null));
    }

    @GetMapping("/{showId}/seats")
    @Operation(summary = "Get live interactive seat layout for a screening with availability and prices (Public)")
    public ResponseEntity<ApiResponse<ShowSeatLayoutResponse>> getShowSeatLayout(
            @PathVariable UUID showId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ShowSeatLayoutResponse response = showSeatLockService.getShowSeatLayout(showId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Show seat layout retrieved successfully", response));
    }

    @PostMapping("/{showId}/lock-seats")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Lock seats for 10 minutes with high-concurrency pessimistic row locking (Customer)")
    public ResponseEntity<ApiResponse<LockSeatsResponse>> lockSeats(
            @PathVariable UUID showId,
            @Valid @RequestBody LockSeatsRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        LockSeatsResponse response = showSeatLockService.lockSeats(showId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Seats locked successfully for 10 minutes", response));
    }

    @PostMapping("/{showId}/unlock-seats")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Voluntarily release locked seats (Customer)")
    public ResponseEntity<ApiResponse<Void>> unlockSeats(
            @PathVariable UUID showId,
            @RequestBody List<UUID> seatIds,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        showSeatLockService.unlockSeats(showId, seatIds, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Seats released successfully", null));
    }
}
