package com.moviefixer.show.scheduler;

import com.moviefixer.show.service.ShowSeatLockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduling.lock-cleaner.enabled", havingValue = "true", matchIfMissing = true)
public class SeatLockCleanupWorker {

    private static final Logger log = LoggerFactory.getLogger(SeatLockCleanupWorker.class);

    private final ShowSeatLockService showSeatLockService;

    public SeatLockCleanupWorker(ShowSeatLockService showSeatLockService) {
        this.showSeatLockService = showSeatLockService;
    }

    /**
     * Executes every 30 seconds to clean up seats whose 10-minute lock TTL has expired.
     */
    @Scheduled(fixedRate = 30000)
    public void cleanupExpiredLocks() {
        try {
            int releasedCount = showSeatLockService.releaseExpiredLocks();
            if (releasedCount > 0) {
                log.debug("Auto-released {} expired seat locks", releasedCount);
            }
        } catch (Exception e) {
            log.error("Error during scheduled seat lock cleanup: {}", e.getMessage(), e);
        }
    }
}
