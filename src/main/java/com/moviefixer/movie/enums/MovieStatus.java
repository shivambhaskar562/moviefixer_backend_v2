package com.moviefixer.movie.enums;

/**
 * Lifecycle status of a movie in the booking platform.
 */
public enum MovieStatus {
    COMING_SOON,   // Announced / Advance booking available
    NOW_SHOWING,   // Active screening in theaters
    ARCHIVED       // Run completed, removed from active bookings
}
