package com.moviefixer.booking.repository;

import com.moviefixer.booking.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    Optional<Ticket> findByQrPayload(String qrPayload);

    List<Ticket> findByBookingId(UUID bookingId);

    @Query("SELECT t FROM Ticket t " +
            "JOIN FETCH t.booking b " +
            "JOIN FETCH b.user u " +
            "JOIN FETCH b.show s " +
            "JOIN FETCH s.movie m " +
            "JOIN FETCH s.screen sc " +
            "JOIN FETCH sc.theater th " +
            "JOIN FETCH t.showSeat ss " +
            "JOIN FETCH ss.seat st " +
            "JOIN FETCH st.tier tr " +
            "WHERE t.ticketNumber = :ticketNumber")
    Optional<Ticket> findByTicketNumberWithDetails(@Param("ticketNumber") String ticketNumber);
}
