package com.moviefixer.movie.service;

import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.movie.dto.GenreRequest;
import com.moviefixer.movie.dto.GenreResponse;
import com.moviefixer.movie.entity.Genre;
import com.moviefixer.movie.repository.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GenreService Unit Tests")
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreServiceImpl genreService;

    private Genre testGenre;
    private UUID genreId;

    @BeforeEach
    void setUp() {
        genreId = UUID.randomUUID();
        testGenre = new Genre(genreId, "Action", "ACTION", "Exciting action sequences", true);
    }

    @Test
    @DisplayName("createGenre successfully persists and returns GenreResponse")
    void createGenre_success() {
        GenreRequest request = new GenreRequest("Action", "ACTION", "Exciting action sequences", true);

        when(genreRepository.existsByCodeIgnoreCase("ACTION")).thenReturn(false);
        when(genreRepository.existsByNameIgnoreCase("Action")).thenReturn(false);
        when(genreRepository.save(any(Genre.class))).thenReturn(testGenre);

        GenreResponse response = genreService.createGenre(request);

        assertNotNull(response);
        assertEquals("Action", response.getName());
        assertEquals("ACTION", response.getCode());
        verify(genreRepository, times(1)).save(any(Genre.class));
    }

    @Test
    @DisplayName("createGenre throws IllegalArgumentException when duplicate code exists")
    void createGenre_duplicateCode_throwsException() {
        GenreRequest request = new GenreRequest("Action", "ACTION", "Desc", true);
        when(genreRepository.existsByCodeIgnoreCase("ACTION")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> genreService.createGenre(request));
        verify(genreRepository, never()).save(any(Genre.class));
    }

    @Test
    @DisplayName("getGenreById returns GenreResponse when found")
    void getGenreById_found() {
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(testGenre));

        GenreResponse response = genreService.getGenreById(genreId);

        assertNotNull(response);
        assertEquals(genreId, response.getId());
        assertEquals("Action", response.getName());
    }

    @Test
    @DisplayName("getGenreById throws ResourceNotFoundException when not found")
    void getGenreById_notFound_throwsException() {
        UUID missingId = UUID.randomUUID();
        when(genreRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> genreService.getGenreById(missingId));
    }

    @Test
    @DisplayName("getAllActiveGenres returns list of active genres")
    void getAllActiveGenres_returnsList() {
        when(genreRepository.findByActiveTrueOrderByNameAsc()).thenReturn(List.of(testGenre));

        List<GenreResponse> result = genreService.getAllActiveGenres();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("ACTION", result.get(0).getCode());
    }
}
