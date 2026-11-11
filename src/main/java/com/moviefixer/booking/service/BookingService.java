package com.moviefixer.booking.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.booking.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookingService {

    /**
     * Initiates a checkout booking session for locked or available seats.
     * Generates a unique booking reference and reserves seats for 10 minutes.
     */
    InitiateBookingResponse initiateBooking(InitiateBookingRequest request, UserPrincipal currentUser);

    /**
     * Confirms an initiated booking using atomic query versioning CAS.
     * Transitions seats to BOOKED without DB row-level locking.
     * Generates cryptographic QR digital passes for all seats.
     */
    BookingResponse confirmBooking(UUID bookingId, UserPrincipal currentUser);

    /**
     * Direct one-step atomic booking flow with OCC query versioning.
     */
    BookingResponse directBooking(DirectBookingRequest request, UserPrincipal currentUser);

    /**
     * Retrieves booking details by ID including tickets and digital QR payload.
     */
    BookingResponse getBookingById(UUID bookingId, UserPrincipal currentUser);

    /**
     * Retrieves paginated booking history for the current authenticated customer.
     */
    Page<BookingResponse> getMyBookings(UserPrincipal currentUser, Pageable pageable);

    /**
     * Cancels a confirmed booking according to the cancellation policy.
     * Releases seats back to AVAILABLE inventory and computes refundable amount.
     */
    CancelBookingResponse cancelBooking(UUID bookingId, UserPrincipal currentUser);

    /**
     * Reclaims expired INITIATED bookings and releases held seats.
     */
    int expireUnconfirmedBookings();
}
