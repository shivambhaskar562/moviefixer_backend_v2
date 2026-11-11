package com.moviefixer.show.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.show.dto.LockSeatsRequest;
import com.moviefixer.show.dto.LockSeatsResponse;
import com.moviefixer.show.dto.ShowSeatLayoutResponse;

import java.util.List;
import java.util.UUID;

public interface ShowSeatLockService {

    ShowSeatLayoutResponse getShowSeatLayout(UUID showId, UserPrincipal currentUser);

    LockSeatsResponse lockSeats(UUID showId, LockSeatsRequest request, UserPrincipal currentUser);

    void unlockSeats(UUID showId, List<UUID> seatIds, UserPrincipal currentUser);

    int releaseExpiredLocks();
}
