package com.moviefixer.movie.controller;

import com.moviefixer.common.dto.ApiResponse;
import com.moviefixer.movie.dto.GenreRequest;
import com.moviefixer.movie.dto.GenreResponse;
import com.moviefixer.movie.service.GenreService;
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
@RequestMapping("/api/genres")
@Tag(name = "Movie Genres", description = "Endpoints for browsing and managing movie genres")
public class GenreController {

    private final GenreService genreService;

    public GenreController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping
    @Operation(summary = "Get all active movie genres")
    public ResponseEntity<ApiResponse<List<GenreResponse>>> getAllActiveGenres() {
        List<GenreResponse> genres = genreService.getAllActiveGenres();
        return ResponseEntity.ok(ApiResponse.success("Active genres fetched successfully", genres));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get genre by ID")
    public ResponseEntity<ApiResponse<GenreResponse>> getGenreById(@PathVariable UUID id) {
        GenreResponse genre = genreService.getGenreById(id);
        return ResponseEntity.ok(ApiResponse.success(genre));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get genre by unique code")
    public ResponseEntity<ApiResponse<GenreResponse>> getGenreByCode(@PathVariable String code) {
        GenreResponse genre = genreService.getGenreByCode(code);
        return ResponseEntity.ok(ApiResponse.success(genre));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create a new movie genre (Admin only)")
    public ResponseEntity<ApiResponse<GenreResponse>> createGenre(@Valid @RequestBody GenreRequest request) {
        GenreResponse created = genreService.createGenre(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Genre created successfully", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update an existing movie genre (Admin only)")
    public ResponseEntity<ApiResponse<GenreResponse>> updateGenre(
            @PathVariable UUID id,
            @Valid @RequestBody GenreRequest request
    ) {
        GenreResponse updated = genreService.updateGenre(id, request);
        return ResponseEntity.ok(ApiResponse.success("Genre updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Deactivate a movie genre (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteGenre(@PathVariable UUID id) {
        genreService.deleteGenre(id);
        return ResponseEntity.ok(ApiResponse.success("Genre deactivated successfully", null));
    }
}
