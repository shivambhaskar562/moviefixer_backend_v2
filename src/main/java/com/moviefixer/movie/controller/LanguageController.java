package com.moviefixer.movie.controller;

import com.moviefixer.common.dto.ApiResponse;
import com.moviefixer.movie.dto.LanguageRequest;
import com.moviefixer.movie.dto.LanguageResponse;
import com.moviefixer.movie.service.LanguageService;
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
@RequestMapping("/api/languages")
@Tag(name = "Movie Languages", description = "Endpoints for browsing and managing screening languages")
public class LanguageController {

    private final LanguageService languageService;

    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    @GetMapping
    @Operation(summary = "Get all active screening languages")
    public ResponseEntity<ApiResponse<List<LanguageResponse>>> getAllActiveLanguages() {
        List<LanguageResponse> languages = languageService.getAllActiveLanguages();
        return ResponseEntity.ok(ApiResponse.success("Active languages fetched successfully", languages));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get language by ID")
    public ResponseEntity<ApiResponse<LanguageResponse>> getLanguageById(@PathVariable UUID id) {
        LanguageResponse language = languageService.getLanguageById(id);
        return ResponseEntity.ok(ApiResponse.success(language));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get language by ISO code (e.g. 'en', 'hi', 'te')")
    public ResponseEntity<ApiResponse<LanguageResponse>> getLanguageByCode(@PathVariable String code) {
        LanguageResponse language = languageService.getLanguageByCode(code);
        return ResponseEntity.ok(ApiResponse.success(language));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new screening language (Admin only)")
    public ResponseEntity<ApiResponse<LanguageResponse>> createLanguage(@Valid @RequestBody LanguageRequest request) {
        LanguageResponse created = languageService.createLanguage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Language created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update an existing screening language (Admin only)")
    public ResponseEntity<ApiResponse<LanguageResponse>> updateLanguage(
            @PathVariable UUID id,
            @Valid @RequestBody LanguageRequest request
    ) {
        LanguageResponse updated = languageService.updateLanguage(id, request);
        return ResponseEntity.ok(ApiResponse.success("Language updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Deactivate a screening language (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteLanguage(@PathVariable UUID id) {
        languageService.deleteLanguage(id);
        return ResponseEntity.ok(ApiResponse.success("Language deactivated successfully", null));
    }
}
