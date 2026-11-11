package com.moviefixer.movie.service;

import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.movie.dto.GenreRequest;
import com.moviefixer.movie.dto.GenreResponse;
import com.moviefixer.movie.entity.Genre;
import com.moviefixer.movie.repository.GenreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class GenreServiceImpl implements GenreService {

    private static final Logger log = LoggerFactory.getLogger(GenreServiceImpl.class);

    private final GenreRepository genreRepository;

    public GenreServiceImpl(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenreResponse> getAllActiveGenres() {
        return genreRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<GenreResponse> getAllGenres() {
        return genreRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GenreResponse getGenreById(UUID id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", id));
        return mapToResponse(genre);
    }

    @Override
    @Transactional(readOnly = true)
    public GenreResponse getGenreByCode(String code) {
        Genre genre = genreRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "code", code));
        return mapToResponse(genre);
    }

    @Override
    @Transactional
    public GenreResponse createGenre(GenreRequest request) {
        String code = request.getCode() != null && !request.getCode().isBlank()
                ? request.getCode().trim().toUpperCase(Locale.ROOT)
                : generateGenreCode(request.getName());

        if (genreRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Genre with code '" + code + "' already exists");
        }
        if (genreRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new IllegalArgumentException("Genre with name '" + request.getName() + "' already exists");
        }

        Genre genre = new Genre(
                request.getName().trim(),
                code,
                request.getDescription()
        );
        genre.setActive(request.isActive());

        Genre saved = genreRepository.save(genre);
        log.info("Created new movie genre: {} [{}]", saved.getName(), saved.getCode());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public GenreResponse updateGenre(UUID id, GenreRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", id));

        if (request.getName() != null && !request.getName().isBlank()) {
            genre.setName(request.getName().trim());
        }
        if (request.getCode() != null && !request.getCode().isBlank()) {
            String newCode = request.getCode().trim().toUpperCase(Locale.ROOT);
            if (!genre.getCode().equalsIgnoreCase(newCode) && genreRepository.existsByCodeIgnoreCase(newCode)) {
                throw new IllegalArgumentException("Genre with code '" + newCode + "' already exists");
            }
            genre.setCode(newCode);
        }
        if (request.getDescription() != null) {
            genre.setDescription(request.getDescription());
        }
        genre.setActive(request.isActive());

        Genre updated = genreRepository.save(genre);
        log.info("Updated genre: {}", updated.getCode());
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteGenre(UUID id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre", "id", id));
        genre.setActive(false);
        genreRepository.save(genre);
        log.info("Deactivated genre ID: {}", id);
    }

    private GenreResponse mapToResponse(Genre genre) {
        return new GenreResponse(
                genre.getId(),
                genre.getName(),
                genre.getCode(),
                genre.getDescription(),
                genre.isActive()
        );
    }

    private String generateGenreCode(String name) {
        return name.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
