package com.moviefixer.movie.controller;

import com.moviefixer.common.dto.ApiResponse;
import com.moviefixer.movie.dto.CreateMovieRequest;
import com.moviefixer.movie.dto.MovieResponse;
import com.moviefixer.movie.dto.MovieSummaryResponse;
import com.moviefixer.movie.dto.UpdateMovieRequest;
import com.moviefixer.movie.enums.MovieStatus;
import com.moviefixer.movie.service.MovieService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
@Tag(name = "Movie Catalog", description = "Endpoints for discovering, filtering, and managing movies")
public class MovieController {

    private final MovieService movieService;

    public MovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    @Operation(summary = "Browse movie catalog with pagination and multi-criteria filters")
    public ResponseEntity<ApiResponse<Page<MovieSummaryResponse>>> getMoviesCatalog(
            @RequestParam(required = false) MovieStatus status,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "releaseDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<MovieSummaryResponse> result = movieService.getMoviesCatalog(status, genre, language, search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Movies retrieved successfully", result));
    }

    @GetMapping("/now-showing")
    @Operation(summary = "Get currently screening movies")
    public ResponseEntity<ApiResponse<List<MovieSummaryResponse>>> getNowShowingMovies() {
        List<MovieSummaryResponse> movies = movieService.getNowShowingMovies();
        return ResponseEntity.ok(ApiResponse.success("Now showing movies retrieved successfully", movies));
    }

    @GetMapping("/upcoming")
    @Operation(summary = "Get coming soon / advance booking movies")
    public ResponseEntity<ApiResponse<List<MovieSummaryResponse>>> getComingSoonMovies() {
        List<MovieSummaryResponse> movies = movieService.getComingSoonMovies();
        return ResponseEntity.ok(ApiResponse.success("Upcoming movies retrieved successfully", movies));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get complete movie details by UUID")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieById(@PathVariable UUID id) {
        MovieResponse movie = movieService.getMovieById(id);
        return ResponseEntity.ok(ApiResponse.success(movie));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get complete movie details by SEO slug")
    public ResponseEntity<ApiResponse<MovieResponse>> getMovieBySlug(@PathVariable String slug) {
        MovieResponse movie = movieService.getMovieBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success(movie));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Register a new movie into the catalog (Admin or Theater Vendor)")
    public ResponseEntity<ApiResponse<MovieResponse>> createMovie(@Valid @RequestBody CreateMovieRequest request) {
        MovieResponse movie = movieService.createMovie(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Movie registered successfully", movie));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_THEATER_VENDOR')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Update an existing movie (Admin or Theater Vendor)")
    public ResponseEntity<ApiResponse<MovieResponse>> updateMovie(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMovieRequest request
    ) {
        MovieResponse updated = movieService.updateMovie(id, request);
        return ResponseEntity.ok(ApiResponse.success("Movie updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Archive a movie (Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable UUID id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(ApiResponse.success("Movie archived successfully", null));
    }
}
