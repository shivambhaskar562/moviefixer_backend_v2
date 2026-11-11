package com.moviefixer.theater.controller;

import com.moviefixer.common.dto.ApiResponse;
import com.moviefixer.theater.dto.SeatingTierRequest;
import com.moviefixer.theater.dto.SeatingTierResponse;
import com.moviefixer.theater.service.SeatingTierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seating-tiers")
@Tag(name = "Seating Tiers", description = "Endpoints for managing seating tiers and price multipliers")
public class SeatingTierController {

    private final SeatingTierService seatingTierService;

    public SeatingTierController(SeatingTierService seatingTierService) {
        this.seatingTierService = seatingTierService;
    }

    @GetMapping
    @Operation(summary = "Get all active seating tiers (Public)")
    public ResponseEntity<ApiResponse<List<SeatingTierResponse>>> getAllActiveTiers() {
        List<SeatingTierResponse> tiers = seatingTierService.getAllActiveTiers();
        return ResponseEntity.ok(ApiResponse.success("Seating tiers retrieved successfully", tiers));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get seating tier details by ID (Public)")
    public ResponseEntity<ApiResponse<SeatingTierResponse>> getTierById(@PathVariable UUID id) {
        SeatingTierResponse tier = seatingTierService.getTierById(id);
        return ResponseEntity.ok(ApiResponse.success(tier));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new seating tier (Admin only)")
    public ResponseEntity<ApiResponse<SeatingTierResponse>> createTier(@Valid @RequestBody SeatingTierRequest request) {
        SeatingTierResponse response = seatingTierService.createTier(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seating tier created successfully", response));
    }
}
