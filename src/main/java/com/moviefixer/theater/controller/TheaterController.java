package com.moviefixer.theater.controller;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.common.dto.ApiResponse;
import com.moviefixer.theater.dto.CreateTheaterRequest;
import com.moviefixer.theater.dto.TheaterResponse;
import com.moviefixer.theater.dto.TheaterSummaryResponse;
import com.moviefixer.theater.dto.UpdateTheaterRequest;
import com.moviefixer.theater.service.TheaterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/theaters")
@Tag(name = "Theater Management", description = "Endpoints for discovering cinemas and multi-vendor theater management")
public class TheaterController {

    private final TheaterService theaterService;

    public TheaterController(TheaterService theaterService) {
        this.theaterService = theaterService;
    }

    @GetMapping
    @Operation(summary = "Browse theaters by city and keyword search (Public)")
    public ResponseEntity<ApiResponse<Page<TheaterSummaryResponse>>> searchTheaters(
            @RequestParam(required = false) UUID cityId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TheaterSummaryResponse> result = theaterService.searchTheaters(cityId, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Theaters retrieved successfully", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get complete theater details by UUID (Public)")
    public ResponseEntity<ApiResponse<TheaterResponse>> getTheaterById(@PathVariable UUID id) {
        TheaterResponse response = theaterService.getTheaterById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "List theaters owned by a specific vendor (Vendor / Admin only)")
    public ResponseEntity<ApiResponse<Page<TheaterResponse>>> getTheatersByVendor(
            @PathVariable UUID vendorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<TheaterResponse> response = theaterService.getTheatersByVendor(vendorId, pageable, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Vendor theaters retrieved successfully", response));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Register a new cinema property (Vendor or Admin)")
    public ResponseEntity<ApiResponse<TheaterResponse>> createTheater(
            @Valid @RequestBody CreateTheaterRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TheaterResponse response = theaterService.createTheater(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Theater registered successfully", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update theater details (Vendor owner or Admin)")
    public ResponseEntity<ApiResponse<TheaterResponse>> updateTheater(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTheaterRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        TheaterResponse response = theaterService.updateTheater(id, request, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Theater updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Soft-archive theater (Vendor owner or Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteTheater(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        theaterService.deleteTheater(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Theater archived successfully", null));
    }
}
