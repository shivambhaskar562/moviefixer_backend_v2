package com.moviefixer.theater.repository;

import com.moviefixer.theater.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, UUID> {

    @Query("SELECT s FROM Seat s JOIN FETCH s.tier WHERE s.screen.id = :screenId ORDER BY s.gridRow ASC, s.gridColumn ASC")
    List<Seat> findByScreenIdOrderByGridRowAscGridColumnAsc(@Param("screenId") UUID screenId);

    @Query("SELECT s FROM Seat s JOIN FETCH s.tier WHERE s.screen.id = :screenId AND s.active = true ORDER BY s.gridRow ASC, s.gridColumn ASC")
    List<Seat> findByScreenIdAndActiveTrueOrderByGridRowAscGridColumnAsc(@Param("screenId") UUID screenId);

    @Query("SELECT s FROM Seat s JOIN FETCH s.tier JOIN FETCH s.screen WHERE s.id = :id")
    Optional<Seat> findByIdWithDetails(@Param("id") UUID id);

    @Modifying
    @Query("DELETE FROM Seat s WHERE s.screen.id = :screenId")
    void deleteByScreenId(@Param("screenId") UUID screenId);

    long countByScreenIdAndActiveTrue(UUID screenId);
}
