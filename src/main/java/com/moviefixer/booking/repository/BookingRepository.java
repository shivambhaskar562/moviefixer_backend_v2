package com.moviefixer.booking.repository;

import com.moviefixer.booking.entity.Booking;
import com.moviefixer.booking.enums.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByBookingReference(String bookingReference);

    boolean existsByBookingReference(String bookingReference);

    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Page<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.user u " +
            "JOIN FETCH b.show s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.screen sc " +
            "JOIN FETCH sc.theater th " +
            "LEFT JOIN FETCH b.tickets t " +
            "LEFT JOIN FETCH t.showSeat ss " +
            "LEFT JOIN FETCH ss.seat st " +
            "LEFT JOIN FETCH st.tier " +
            "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") UUID id);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.user u " +
            "JOIN FETCH b.show s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.screen sc " +
            "JOIN FETCH sc.theater th " +
            "LEFT JOIN FETCH b.tickets t " +
            "LEFT JOIN FETCH t.showSeat ss " +
            "LEFT JOIN FETCH ss.seat st " +
            "LEFT JOIN FETCH st.tier " +
            "WHERE b.bookingReference = :bookingReference")
    Optional<Booking> findByBookingReferenceWithDetails(@Param("bookingReference") String bookingReference);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.expiresAt < :now")
    List<Booking> findExpiredBookings(
            @Param("status") BookingStatus status,
            @Param("now") LocalDateTime now
    );
}
