package com.moviefixer.movie.service;

import com.moviefixer.movie.dto.CreateMovieRequest;
import com.moviefixer.movie.dto.MovieResponse;
import com.moviefixer.movie.dto.MovieSummaryResponse;
import com.moviefixer.movie.dto.UpdateMovieRequest;
import com.moviefixer.movie.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface MovieService {

    Page<MovieSummaryResponse> getMoviesCatalog(
            MovieStatus status,
            String genreCode,
            String languageCode,
            String keyword,
            Pageable pageable
    );

    List<MovieSummaryResponse> getNowShowingMovies();

    List<MovieSummaryResponse> getComingSoonMovies();

    MovieResponse getMovieById(UUID id);

    MovieResponse getMovieBySlug(String slug);

    MovieResponse createMovie(CreateMovieRequest request);

    MovieResponse updateMovie(UUID id, UpdateMovieRequest request);

    void deleteMovie(UUID id);
}
