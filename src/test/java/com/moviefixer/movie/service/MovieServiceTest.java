package com.moviefixer.movie.service;

import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.movie.dto.CreateMovieRequest;
import com.moviefixer.movie.dto.MovieResponse;
import com.moviefixer.movie.dto.MovieSummaryResponse;
import com.moviefixer.movie.dto.UpdateMovieRequest;
import com.moviefixer.movie.entity.Genre;
import com.moviefixer.movie.entity.Language;
import com.moviefixer.movie.entity.Movie;
import com.moviefixer.movie.enums.ContentRating;
import com.moviefixer.movie.enums.MovieStatus;
import com.moviefixer.movie.repository.GenreRepository;
import com.moviefixer.movie.repository.LanguageRepository;
import com.moviefixer.movie.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService Unit Tests")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private LanguageRepository languageRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    private UUID movieId;
    private UUID genreId;
    private UUID languageId;
    private Genre testGenre;
    private Language testLanguage;
    private Movie testMovie;

    @BeforeEach
    void setUp() {
        movieId = UUID.randomUUID();
        genreId = UUID.randomUUID();
        languageId = UUID.randomUUID();

        testGenre = new Genre(genreId, "Sci-Fi", "SCI_FI", "Science Fiction", true);
        testLanguage = new Language(languageId, "English", "en", true);

        testMovie = new Movie();
        testMovie.setId(movieId);
        testMovie.setTitle("Inception");
        testMovie.setSlug("inception");
        testMovie.setSynopsis("A thief who steals corporate secrets through dream-sharing.");
        testMovie.setDurationMinutes(148);
        testMovie.setReleaseDate(LocalDate.of(2010, 7, 16));
        testMovie.setPosterUrl("https://example.com/inception-poster.jpg");
        testMovie.setBannerUrl("https://example.com/inception-banner.jpg");
        testMovie.setTrailerUrl("https://example.com/inception-trailer.mp4");
        testMovie.setContentRating(ContentRating.UA_13_PLUS);
        testMovie.setStatus(MovieStatus.NOW_SHOWING);
        testMovie.setGenres(new HashSet<>(Set.of(testGenre)));
        testMovie.setLanguages(new HashSet<>(Set.of(testLanguage)));
        testMovie.setCreatedAt(Instant.now());
        testMovie.setUpdatedAt(Instant.now());
    }

    @Test
    @DisplayName("getMovieById returns MovieResponse when found")
    void getMovieById_found() {
        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(testMovie));

        MovieResponse response = movieService.getMovieById(movieId);

        assertNotNull(response);
        assertEquals(movieId, response.getId());
        assertEquals("Inception", response.getTitle());
        assertEquals("inception", response.getSlug());
        assertEquals(1, response.getGenres().size());
        assertEquals("Sci-Fi", response.getGenres().get(0).getName());
        assertEquals(1, response.getLanguages().size());
        assertEquals("English", response.getLanguages().get(0).getName());
    }

    @Test
    @DisplayName("getMovieById throws ResourceNotFoundException when not found")
    void getMovieById_notFound_throwsException() {
        UUID unknownId = UUID.randomUUID();
        when(movieRepository.findByIdWithDetails(unknownId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieById(unknownId));
    }

    @Test
    @DisplayName("getMovieBySlug returns MovieResponse when found")
    void getMovieBySlug_found() {
        when(movieRepository.findBySlugWithDetails("inception")).thenReturn(Optional.of(testMovie));

        MovieResponse response = movieService.getMovieBySlug("inception");

        assertNotNull(response);
        assertEquals("Inception", response.getTitle());
        assertEquals("inception", response.getSlug());
    }

    @Test
    @DisplayName("getMovieBySlug throws ResourceNotFoundException when not found")
    void getMovieBySlug_notFound_throwsException() {
        when(movieRepository.findBySlugWithDetails("unknown-slug")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.getMovieBySlug("unknown-slug"));
    }

    @Test
    @DisplayName("createMovie auto-generates slug and persists movie successfully")
    void createMovie_autoSlug_success() {
        CreateMovieRequest request = new CreateMovieRequest();
        request.setTitle("Interstellar");
        request.setSynopsis("Explorers travel through a wormhole in space.");
        request.setDurationMinutes(169);
        request.setReleaseDate(LocalDate.now().plusMonths(3));
        request.setContentRating(ContentRating.UA_13_PLUS);

        request.setGenreIds(Set.of(genreId));
        request.setLanguageIds(Set.of(languageId));

        when(movieRepository.existsBySlug("interstellar")).thenReturn(false);
        when(genreRepository.findAllById(Set.of(genreId))).thenReturn(List.of(testGenre));
        when(languageRepository.findAllById(Set.of(languageId))).thenReturn(List.of(testLanguage));

        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie m = invocation.getArgument(0);
            m.setId(UUID.randomUUID());
            m.setCreatedAt(Instant.now());
            m.setUpdatedAt(Instant.now());
            return m;
        });

        MovieResponse response = movieService.createMovie(request);

        assertNotNull(response);
        assertEquals("Interstellar", response.getTitle());
        assertEquals("interstellar", response.getSlug());
        assertEquals(MovieStatus.COMING_SOON, response.getStatus()); // since release date is future
        assertEquals(1, response.getGenres().size());
        assertEquals(1, response.getLanguages().size());

        ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(captor.capture());
        Movie savedMovie = captor.getValue();
        assertEquals("interstellar", savedMovie.getSlug());
    }

    @Test
    @DisplayName("createMovie generates unique slug with suffix when base slug exists")
    void createMovie_duplicateBaseSlug_appendsCounter() {
        CreateMovieRequest request = new CreateMovieRequest();
        request.setTitle("Inception");
        request.setDurationMinutes(148);
        request.setReleaseDate(LocalDate.of(2010, 7, 16));
        request.setContentRating(ContentRating.UA_13_PLUS);
        request.setGenreIds(Set.of(genreId));
        request.setLanguageIds(Set.of(languageId));

        when(movieRepository.existsBySlug("inception")).thenReturn(true);
        when(movieRepository.existsBySlug("inception-1")).thenReturn(false);
        when(genreRepository.findAllById(Set.of(genreId))).thenReturn(List.of(testGenre));
        when(languageRepository.findAllById(Set.of(languageId))).thenReturn(List.of(testLanguage));

        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie m = invocation.getArgument(0);
            m.setId(UUID.randomUUID());
            return m;
        });

        MovieResponse response = movieService.createMovie(request);

        assertNotNull(response);
        assertEquals("inception-1", response.getSlug());
    }

    @Test
    @DisplayName("createMovie throws IllegalArgumentException when custom slug is duplicate")
    void createMovie_duplicateCustomSlug_throwsException() {
        CreateMovieRequest request = new CreateMovieRequest();
        request.setTitle("Inception");
        request.setSlug("custom-inception");
        request.setDurationMinutes(148);
        request.setReleaseDate(LocalDate.of(2010, 7, 16));
        request.setContentRating(ContentRating.UA_13_PLUS);
        request.setGenreIds(Set.of(genreId));
        request.setLanguageIds(Set.of(languageId));

        when(movieRepository.existsBySlug("custom-inception")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> movieService.createMovie(request));
        verify(movieRepository, never()).save(any());
    }

    @Test
    @DisplayName("createMovie throws ResourceNotFoundException when genre is missing")
    void createMovie_invalidGenre_throwsException() {
        UUID missingGenreId = UUID.randomUUID();
        CreateMovieRequest request = new CreateMovieRequest();
        request.setTitle("Inception");
        request.setDurationMinutes(148);
        request.setReleaseDate(LocalDate.of(2010, 7, 16));
        request.setContentRating(ContentRating.UA_13_PLUS);
        request.setGenreIds(Set.of(missingGenreId));
        request.setLanguageIds(Set.of(languageId));

        when(movieRepository.existsBySlug("inception")).thenReturn(false);
        when(genreRepository.findAllById(Set.of(missingGenreId))).thenReturn(Collections.emptyList());

        assertThrows(ResourceNotFoundException.class, () -> movieService.createMovie(request));
        verify(movieRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateMovie successfully modifies fields and updates relationships")
    void updateMovie_success() {
        UpdateMovieRequest updateRequest = new UpdateMovieRequest();
        updateRequest.setTitle("Inception: IMAX Edition");
        updateRequest.setStatus(MovieStatus.ARCHIVED);

        when(movieRepository.findByIdWithDetails(movieId)).thenReturn(Optional.of(testMovie));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MovieResponse response = movieService.updateMovie(movieId, updateRequest);

        assertNotNull(response);
        assertEquals("Inception: IMAX Edition", response.getTitle());
        assertEquals(MovieStatus.ARCHIVED, response.getStatus());
        verify(movieRepository).save(testMovie);
    }

    @Test
    @DisplayName("deleteMovie marks status as ARCHIVED")
    void deleteMovie_archivesMovie() {
        when(movieRepository.findById(movieId)).thenReturn(Optional.of(testMovie));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        movieService.deleteMovie(movieId);

        assertEquals(MovieStatus.ARCHIVED, testMovie.getStatus());
        verify(movieRepository).save(testMovie);
    }

    @Test
    @DisplayName("getMoviesCatalog returns paginated summary responses")
    void getMoviesCatalog_returnsPagedSummaries() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> moviePage = new PageImpl<>(List.of(testMovie), pageable, 1);

        when(movieRepository.searchMovies(eq(MovieStatus.NOW_SHOWING), eq("SCI_FI"), eq("en"), eq("Inception"), eq(pageable)))
                .thenReturn(moviePage);

        Page<MovieSummaryResponse> result = movieService.getMoviesCatalog(
                MovieStatus.NOW_SHOWING, "SCI_FI", "en", "Inception", pageable
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        MovieSummaryResponse summary = result.getContent().get(0);
        assertEquals("Inception", summary.getTitle());
        assertEquals(1, summary.getGenreNames().size());
        assertEquals("Sci-Fi", summary.getGenreNames().get(0));

    }

    @Test
    @DisplayName("getNowShowingMovies returns list of summary responses")
    void getNowShowingMovies_returnsList() {
        when(movieRepository.findByStatusOrderByReleaseDateDesc(MovieStatus.NOW_SHOWING))
                .thenReturn(List.of(testMovie));

        List<MovieSummaryResponse> list = movieService.getNowShowingMovies();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Inception", list.get(0).getTitle());
    }

    @Test
    @DisplayName("getComingSoonMovies returns list of summary responses")
    void getComingSoonMovies_returnsList() {
        testMovie.setStatus(MovieStatus.COMING_SOON);
        when(movieRepository.findByStatusOrderByReleaseDateDesc(MovieStatus.COMING_SOON))
                .thenReturn(List.of(testMovie));

        List<MovieSummaryResponse> list = movieService.getComingSoonMovies();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("Inception", list.get(0).getTitle());
    }
}
