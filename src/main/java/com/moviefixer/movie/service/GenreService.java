package com.moviefixer.movie.service;

import com.moviefixer.movie.dto.GenreRequest;
import com.moviefixer.movie.dto.GenreResponse;

import java.util.List;
import java.util.UUID;

public interface GenreService {

    List<GenreResponse> getAllActiveGenres();

    List<GenreResponse> getAllGenres();

    GenreResponse getGenreById(UUID id);

    GenreResponse getGenreByCode(String code);

    GenreResponse createGenre(GenreRequest request);

    GenreResponse updateGenre(UUID id, GenreRequest request);

    void deleteGenre(UUID id);
}
