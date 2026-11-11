package com.moviefixer.theater.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.theater.dto.BatchSeatLayoutRequest;
import com.moviefixer.theater.dto.GenerateGridRequest;
import com.moviefixer.theater.dto.ScreenLayoutResponse;
import com.moviefixer.theater.dto.SeatResponse;

import java.util.UUID;

public interface SeatLayoutService {

    ScreenLayoutResponse configureBatchLayout(UUID screenId, BatchSeatLayoutRequest request, UserPrincipal currentUser);

    ScreenLayoutResponse generateGridLayout(UUID screenId, GenerateGridRequest request, UserPrincipal currentUser);

    ScreenLayoutResponse getScreenLayout(UUID screenId);

    SeatResponse toggleSeatStatus(UUID seatId, boolean active, UserPrincipal currentUser);
}
