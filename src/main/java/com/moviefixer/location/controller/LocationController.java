package com.moviefixer.location.controller;

import com.moviefixer.location.dto.CitySummaryResponse;
import com.moviefixer.location.entity.Country;
import com.moviefixer.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@Tag(name = "Location Service", description = "Reusable geographic data (cities, states, countries) for theater discovery, registration, and analytics")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @Operation(summary = "Get all available cities", description = "Returns cities with state and country data for theater discovery and user preference selection")
    @GetMapping("/cities")
    public ResponseEntity<List<CitySummaryResponse>> getAllCities() {
        return ResponseEntity.ok(locationService.getAllCities());
    }

    @Operation(summary = "Get all countries", description = "Returns all supported countries")
    @GetMapping("/countries")
    public ResponseEntity<List<Country>> getAllCountries() {
        return ResponseEntity.ok(locationService.getAllCountries());
    }
}
