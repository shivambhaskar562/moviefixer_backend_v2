package com.moviefixer.show.repository;

import com.moviefixer.show.entity.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShowRepository extends JpaRepository<Show, UUID> {

    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.screen sc " +
            "JOIN FETCH sc.theater t " +
            "JOIN FETCH t.city c " +
            "WHERE s.id = :id")
    Optional<Show> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT s FROM Show s " +
            "WHERE s.screen.id = :screenId " +
            "AND s.status != 'CANCELLED' " +
            "AND s.startTime < :endWithBuffer " +
            "AND s.endTime > :startWithBuffer " +
            "AND (:excludeShowId IS NULL OR s.id != :excludeShowId)")
    List<Show> findCollisions(
            @Param("screenId") UUID screenId,
            @Param("startWithBuffer") LocalDateTime startWithBuffer,
            @Param("endWithBuffer") LocalDateTime endWithBuffer,
            @Param("excludeShowId") UUID excludeShowId
    );

    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.screen sc " +
            "JOIN FETCH sc.theater t " +
            "JOIN FETCH t.city c " +
            "WHERE m.id = :movieId AND c.id = :cityId " +
            "AND s.startTime >= :startOfDay AND s.startTime <= :endOfDay " +
            "AND s.status = 'SCHEDULED' " +
            "ORDER BY t.name ASC, sc.name ASC, s.startTime ASC")
    List<Show> findShowsByMovieAndCityAndDate(
            @Param("movieId") UUID movieId,
            @Param("cityId") UUID cityId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT s FROM Show s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.screen sc " +
            "WHERE sc.theater.id = :theaterId " +
            "AND s.startTime >= :startOfDay AND s.startTime <= :endOfDay " +
            "ORDER BY sc.name ASC, s.startTime ASC")
    List<Show> findShowsByTheaterAndDate(
            @Param("theaterId") UUID theaterId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    List<Show> findByScreenIdAndStartTimeBetweenOrderByStartTimeAsc(
            UUID screenId, LocalDateTime start, LocalDateTime end
    );
}
