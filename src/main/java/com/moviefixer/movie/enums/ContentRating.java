package com.moviefixer.movie.enums;

/**
 * Standardized Content Rating / Age Certification for Movies.
 */
public enum ContentRating {
    U,             // Unrestricted Public Exhibition (All ages)
    UA_13_PLUS,    // Parental Guidance for children below 13
    UA_16_PLUS,    // Parental Guidance for children below 16
    A,             // Adults Only (18+)
    PG_13,         // Parents Strongly Cautioned
    R              // Restricted (Under 17 requires accompanying parent)
}
