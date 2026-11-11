package com.moviefixer.show.repository;

import com.moviefixer.show.entity.ShowSeat;
import com.moviefixer.show.enums.ShowSeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ShowSeatRepository extends JpaRepository<ShowSeat, UUID> {

    @Query("SELECT ss FROM ShowSeat ss " +
            "JOIN FETCH ss.seat s " +
            "JOIN FETCH s.tier " +
            "WHERE ss.show.id = :showId AND ss.seat.id IN :seatIds")
    List<ShowSeat> findByShowIdAndSeatIdIn(
            @Param("showId") UUID showId,
            @Param("seatIds") List<UUID> seatIds
    );

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = 'LOCKED', ss.lockedByUserId = :userId, " +
            "ss.lockedUntil = :lockedUntil, ss.version = ss.version + 1 " +
            "WHERE ss.id = :showSeatId AND ss.version = :expectedVersion " +
            "AND (ss.status = 'AVAILABLE' OR (ss.status = 'LOCKED' AND ss.lockedUntil < :now))")
    int atomicLockSeatWithVersion(
            @Param("showSeatId") UUID showSeatId,
            @Param("expectedVersion") Long expectedVersion,
            @Param("userId") UUID userId,
            @Param("lockedUntil") LocalDateTime lockedUntil,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT ss FROM ShowSeat ss " +
            "JOIN FETCH ss.seat s " +
            "JOIN FETCH s.tier " +
            "WHERE ss.show.id = :showId " +
            "ORDER BY s.gridRow ASC, s.gridColumn ASC")
    List<ShowSeat> findByShowIdWithSeatDetails(@Param("showId") UUID showId);

    long countByShowIdAndStatus(UUID showId, ShowSeatStatus status);

    long countByShowId(UUID showId);

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = 'AVAILABLE', ss.lockedByUserId = NULL, ss.lockedUntil = NULL " +
            "WHERE ss.status = 'LOCKED' AND ss.lockedUntil < :now")
    int releaseExpiredLocks(@Param("now") LocalDateTime now);

    List<ShowSeat> findByShowIdAndLockedByUserIdAndStatus(UUID showId, UUID userId, ShowSeatStatus status);

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = 'BOOKED', ss.lockedByUserId = NULL, ss.lockedUntil = NULL, ss.version = ss.version + 1 " +
            "WHERE ss.id = :showSeatId AND ss.version = :expectedVersion " +
            "AND ((ss.status = 'LOCKED' AND ss.lockedByUserId = :userId) OR ss.status = 'AVAILABLE')")
    int atomicBookSeatWithVersion(
            @Param("showSeatId") UUID showSeatId,
            @Param("expectedVersion") Long expectedVersion,
            @Param("userId") UUID userId
    );

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = 'AVAILABLE', ss.lockedByUserId = NULL, ss.lockedUntil = NULL, ss.version = ss.version + 1 " +
            "WHERE ss.id = :showSeatId AND ss.status = 'BOOKED'")
    int atomicReleaseBookedSeat(@Param("showSeatId") UUID showSeatId);

    @Modifying
    @Query("UPDATE ShowSeat ss SET ss.status = 'AVAILABLE', ss.lockedByUserId = NULL, ss.lockedUntil = NULL, ss.version = ss.version + 1 " +
            "WHERE ss.id = :showSeatId AND ss.status = 'LOCKED' AND ss.lockedByUserId = :userId")
    int atomicReleaseLockedSeat(
            @Param("showSeatId") UUID showSeatId,
            @Param("userId") UUID userId
    );
}

