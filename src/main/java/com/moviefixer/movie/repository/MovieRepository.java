package com.moviefixer.movie.repository;

import com.moviefixer.movie.entity.Movie;
import com.moviefixer.movie.enums.MovieStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MovieRepository extends JpaRepository<Movie, UUID> {

    Optional<Movie> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByTitleIgnoreCase(String title);

    List<Movie> findByStatusOrderByReleaseDateDesc(MovieStatus status);

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.genres LEFT JOIN FETCH m.languages WHERE m.id = :id")
    Optional<Movie> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.genres LEFT JOIN FETCH m.languages WHERE m.slug = :slug")
    Optional<Movie> findBySlugWithDetails(@Param("slug") String slug);

    @Query("""
        SELECT DISTINCT m FROM Movie m
        LEFT JOIN m.genres g
        LEFT JOIN m.languages l
        WHERE (:status IS NULL OR m.status = :status)
          AND (:genreCode IS NULL OR LOWER(g.code) = LOWER(:genreCode))
          AND (:languageCode IS NULL OR LOWER(l.code) = LOWER(:languageCode))
          AND (:keyword IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    Page<Movie> searchMovies(
            @Param("status") MovieStatus status,
            @Param("genreCode") String genreCode,
            @Param("languageCode") String languageCode,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
