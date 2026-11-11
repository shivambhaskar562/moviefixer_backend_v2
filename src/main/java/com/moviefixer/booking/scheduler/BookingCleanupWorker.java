package com.moviefixer.booking.scheduler;

import com.moviefixer.booking.service.BookingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BookingCleanupWorker {

    private static final Logger log = LoggerFactory.getLogger(BookingCleanupWorker.class);

    private final BookingService bookingService;

    public BookingCleanupWorker(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Executes every 30 seconds to reclaim expired INITIATED checkout sessions
     * and restore held seats back to the AVAILABLE inventory.
     */
    @Scheduled(fixedDelay = 30000, initialDelay = 15000)
    public void cleanupExpiredBookings() {
        try {
            int reclaimed = bookingService.expireUnconfirmedBookings();
            if (reclaimed > 0) {
                log.info("BookingCleanupWorker successfully reclaimed {} expired bookings", reclaimed);
            }
        } catch (Exception e) {
            log.error("Error during scheduled booking expiration cleanup", e);
        }
    }
}
