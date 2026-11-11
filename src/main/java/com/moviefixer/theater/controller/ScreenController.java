package com.moviefixer.theater.controller;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.common.dto.ApiResponse;
import com.moviefixer.theater.dto.*;
import com.moviefixer.theater.service.ScreenService;
import com.moviefixer.theater.service.SeatLayoutService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "Screens & Seating Layouts", description = "Endpoints for auditoriums, projection formats, and dynamic seating grids")
public class ScreenController {

    private final ScreenService screenService;
    private final SeatLayoutService seatLayoutService;

    public ScreenController(ScreenService screenService, SeatLayoutService seatLayoutService) {
        this.screenService = screenService;
        this.seatLayoutService = seatLayoutService;
    }

    @GetMapping("/api/theaters/{theaterId}/screens")
    @Operation(summary = "List all screens in a theater (Public)")
    public ResponseEntity<ApiResponse<List<ScreenResponse>>> getScreensByTheater(@PathVariable UUID theaterId) {
        List<ScreenResponse> screens = screenService.getScreensByTheater(theaterId);
        return ResponseEntity.ok(ApiResponse.success("Screens retrieved successfully", screens));
    }

    @PostMapping("/api/theaters/{theaterId}/screens")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Register a new screen / auditorium in a theater (Vendor / Admin)")
    public ResponseEntity<ApiResponse<ScreenResponse>> createScreen(
            @PathVariable UUID theaterId,
            @Valid @RequestBody CreateScreenRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ScreenResponse screen = screenService.createScreen(theaterId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Screen created successfully", screen));
    }

    @GetMapping("/api/screens/{screenId}")
    @Operation(summary = "Get screen details by ID (Public)")
    public ResponseEntity<ApiResponse<ScreenResponse>> getScreenById(@PathVariable UUID screenId) {
        ScreenResponse screen = screenService.getScreenById(screenId);
        return ResponseEntity.ok(ApiResponse.success(screen));
    }

    @PutMapping("/api/screens/{screenId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update screen projection or audio formats (Vendor / Admin)")
    public ResponseEntity<ApiResponse<ScreenResponse>> updateScreen(
            @PathVariable UUID screenId,
            @Valid @RequestBody UpdateScreenRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ScreenResponse updated = screenService.updateScreen(screenId, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Screen updated successfully", updated));
    }

    @DeleteMapping("/api/screens/{screenId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Soft-archive screen (Vendor / Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteScreen(
            @PathVariable UUID screenId,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        screenService.deleteScreen(screenId, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Screen archived successfully", null));
    }

    @GetMapping("/api/screens/{screenId}/layout")
    @Operation(summary = "Get 2D dynamic seating layout for a screen (Public / Booking)")
    public ResponseEntity<ApiResponse<ScreenLayoutResponse>> getScreenLayout(@PathVariable UUID screenId) {
        ScreenLayoutResponse layout = seatLayoutService.getScreenLayout(screenId);
        return ResponseEntity.ok(ApiResponse.success("Screen layout retrieved successfully", layout));
    }

    @PostMapping("/api/screens/{screenId}/layout")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Configure custom 2D seating grid for a screen (Vendor / Admin)")
    public ResponseEntity<ApiResponse<ScreenLayoutResponse>> configureBatchLayout(
            @PathVariable UUID screenId,
            @Valid @RequestBody BatchSeatLayoutRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ScreenLayoutResponse layout = seatLayoutService.configureBatchLayout(screenId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seat layout configured successfully", layout));
    }

    @PostMapping("/api/screens/{screenId}/layout/generate")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Quickly auto-generate standard NxM seating grid with tiers (Vendor / Admin)")
    public ResponseEntity<ApiResponse<ScreenLayoutResponse>> generateGridLayout(
            @PathVariable UUID screenId,
            @Valid @RequestBody GenerateGridRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        ScreenLayoutResponse layout = seatLayoutService.generateGridLayout(screenId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seat grid generated successfully", layout));
    }

    @PatchMapping("/api/seats/{seatId}/toggle-active")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Toggle physical seat availability (Maintenance / Broken Seat)")
    public ResponseEntity<ApiResponse<SeatResponse>> toggleSeatStatus(
            @PathVariable UUID seatId,
            @RequestParam boolean active,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        SeatResponse response = seatLayoutService.toggleSeatStatus(seatId, active, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Seat status updated successfully", response));
    }
}
