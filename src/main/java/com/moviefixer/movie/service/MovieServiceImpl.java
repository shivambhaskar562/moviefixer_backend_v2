package com.moviefixer.movie.service;

import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.movie.dto.*;
import com.moviefixer.movie.entity.Genre;
import com.moviefixer.movie.entity.Language;
import com.moviefixer.movie.entity.Movie;
import com.moviefixer.movie.enums.MovieStatus;
import com.moviefixer.movie.repository.GenreRepository;
import com.moviefixer.movie.repository.LanguageRepository;
import com.moviefixer.movie.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {

    private static final Logger log = LoggerFactory.getLogger(MovieServiceImpl.class);

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final LanguageRepository languageRepository;

    public MovieServiceImpl(
            MovieRepository movieRepository,
            GenreRepository genreRepository,
            LanguageRepository languageRepository
    ) {
        this.movieRepository = movieRepository;
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieSummaryResponse> getMoviesCatalog(
            MovieStatus status,
            String genreCode,
            String languageCode,
            String keyword,
            Pageable pageable
    ) {
        String cleanKeyword = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String cleanGenre = (genreCode != null && !genreCode.isBlank()) ? genreCode.trim() : null;
        String cleanLanguage = (languageCode != null && !languageCode.isBlank()) ? languageCode.trim() : null;

        return movieRepository.searchMovies(status, cleanGenre, cleanLanguage, cleanKeyword, pageable)
                .map(this::mapToSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieSummaryResponse> getNowShowingMovies() {
        return movieRepository.findByStatusOrderByReleaseDateDesc(MovieStatus.NOW_SHOWING)
                .stream()
                .map(this::mapToSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieSummaryResponse> getComingSoonMovies() {
        return movieRepository.findByStatusOrderByReleaseDateDesc(MovieStatus.COMING_SOON)
                .stream()
                .map(this::mapToSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getMovieById(UUID id) {
        Movie movie = movieRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        return mapToResponse(movie);
    }

    @Override
    @Transactional(readOnly = true)
    public MovieResponse getMovieBySlug(String slug) {
        Movie movie = movieRepository.findBySlugWithDetails(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "slug", slug));
        return mapToResponse(movie);
    }

    @Override
    @Transactional
    public MovieResponse createMovie(CreateMovieRequest request) {
        String slug = (request.getSlug() != null && !request.getSlug().isBlank())
                ? sanitizeSlug(request.getSlug())
                : generateUniqueSlug(request.getTitle());

        if (movieRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Movie with slug '" + slug + "' already exists");
        }

        Set<Genre> genres = resolveGenres(request.getGenreIds());
        Set<Language> languages = resolveLanguages(request.getLanguageIds());

        MovieStatus status = request.getStatus();
        if (status == null) {
            status = request.getReleaseDate().isAfter(LocalDate.now())
                    ? MovieStatus.COMING_SOON
                    : MovieStatus.NOW_SHOWING;
        }

        Movie movie = new Movie();
        movie.setTitle(request.getTitle().trim());
        movie.setSlug(slug);
        movie.setSynopsis(request.getSynopsis());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setBannerUrl(request.getBannerUrl());
        movie.setTrailerUrl(request.getTrailerUrl());
        movie.setContentRating(request.getContentRating());
        movie.setStatus(status);
        movie.setGenres(genres);
        movie.setLanguages(languages);

        Movie saved = movieRepository.save(movie);
        log.info("Successfully registered movie: {} [ID: {}]", saved.getTitle(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public MovieResponse updateMovie(UUID id, UpdateMovieRequest request) {
        Movie movie = movieRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            movie.setTitle(request.getTitle().trim());
        }

        if (request.getSlug() != null && !request.getSlug().isBlank()) {
            String newSlug = sanitizeSlug(request.getSlug());
            if (!movie.getSlug().equalsIgnoreCase(newSlug) && movieRepository.existsBySlug(newSlug)) {
                throw new IllegalArgumentException("Movie with slug '" + newSlug + "' already exists");
            }
            movie.setSlug(newSlug);
        }

        if (request.getSynopsis() != null) {
            movie.setSynopsis(request.getSynopsis());
        }
        if (request.getDurationMinutes() != null) {
            movie.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getReleaseDate() != null) {
            movie.setReleaseDate(request.getReleaseDate());
        }
        if (request.getPosterUrl() != null) {
            movie.setPosterUrl(request.getPosterUrl());
        }
        if (request.getBannerUrl() != null) {
            movie.setBannerUrl(request.getBannerUrl());
        }
        if (request.getTrailerUrl() != null) {
            movie.setTrailerUrl(request.getTrailerUrl());
        }
        if (request.getContentRating() != null) {
            movie.setContentRating(request.getContentRating());
        }
        if (request.getStatus() != null) {
            movie.setStatus(request.getStatus());
        }

        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            movie.setGenres(resolveGenres(request.getGenreIds()));
        }
        if (request.getLanguageIds() != null && !request.getLanguageIds().isEmpty()) {
            movie.setLanguages(resolveLanguages(request.getLanguageIds()));
        }

        Movie updated = movieRepository.save(movie);
        log.info("Updated movie details: {} [ID: {}]", updated.getTitle(), updated.getId());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteMovie(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        movie.setStatus(MovieStatus.ARCHIVED);
        movieRepository.save(movie);
        log.info("Archived movie ID: {} ({})", id, movie.getTitle());
    }

    private Set<Genre> resolveGenres(Set<UUID> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Genre> genres = genreRepository.findAllById(genreIds);
        if (genres.size() != genreIds.size()) {
            Set<UUID> foundIds = genres.stream().map(Genre::getId).collect(Collectors.toSet());
            UUID missingId = genreIds.stream().filter(id -> !foundIds.contains(id)).findFirst().orElse(null);
            throw new ResourceNotFoundException("Genre", "id", missingId);
        }
        return new HashSet<>(genres);
    }

    private Set<Language> resolveLanguages(Set<UUID> languageIds) {
        if (languageIds == null || languageIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Language> languages = languageRepository.findAllById(languageIds);
        if (languages.size() != languageIds.size()) {
            Set<UUID> foundIds = languages.stream().map(Language::getId).collect(Collectors.toSet());
            UUID missingId = languageIds.stream().filter(id -> !foundIds.contains(id)).findFirst().orElse(null);
            throw new ResourceNotFoundException("Language", "id", missingId);
        }
        return new HashSet<>(languages);
    }

    private MovieResponse mapToResponse(Movie movie) {
        List<GenreResponse> genres = movie.getGenres().stream()
                .map(g -> new GenreResponse(
                        g.getId(),
                        g.getName(),
                        g.getCode(),
                        g.getDescription(),
                        g.isActive()
                ))
                .toList();

        List<LanguageResponse> languages = movie.getLanguages().stream()
                .map(l -> new LanguageResponse(
                        l.getId(),
                        l.getName(),
                        l.getCode(),
                        l.isActive()
                ))
                .toList();

        MovieResponse response = new MovieResponse();
        response.setId(movie.getId());
        response.setTitle(movie.getTitle());
        response.setSlug(movie.getSlug());
        response.setSynopsis(movie.getSynopsis());
        response.setDurationMinutes(movie.getDurationMinutes());
        response.setReleaseDate(movie.getReleaseDate());
        response.setPosterUrl(movie.getPosterUrl());
        response.setBannerUrl(movie.getBannerUrl());
        response.setTrailerUrl(movie.getTrailerUrl());
        response.setContentRating(movie.getContentRating());
        response.setStatus(movie.getStatus());
        response.setGenres(genres);
        response.setLanguages(languages);
        response.setCreatedAt(movie.getCreatedAt());
        response.setUpdatedAt(movie.getUpdatedAt());

        return response;
    }

    private MovieSummaryResponse mapToSummaryResponse(Movie movie) {
        List<String> genreNames = movie.getGenres().stream().map(Genre::getName).sorted().toList();
        List<String> languageNames = movie.getLanguages().stream().map(Language::getName).sorted().toList();

        return new MovieSummaryResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getSlug(),
                movie.getDurationMinutes(),
                movie.getReleaseDate(),
                movie.getPosterUrl(),
                movie.getContentRating(),
                movie.getStatus(),
                genreNames,
                languageNames
        );
    }

    private String generateUniqueSlug(String title) {
        String base = sanitizeSlug(title);
        String candidate = base;
        int counter = 1;
        while (movieRepository.existsBySlug(candidate)) {
            candidate = base + "-" + counter++;
        }
        return candidate;
    }

    private String sanitizeSlug(String text) {
        return text.trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
