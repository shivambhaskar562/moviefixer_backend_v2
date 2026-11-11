package com.moviefixer.theater.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.theater.dto.CreateScreenRequest;
import com.moviefixer.theater.dto.ScreenResponse;
import com.moviefixer.theater.dto.UpdateScreenRequest;

import java.util.List;
import java.util.UUID;

public interface ScreenService {

    ScreenResponse createScreen(UUID theaterId, CreateScreenRequest request, UserPrincipal currentUser);

    ScreenResponse updateScreen(UUID screenId, UpdateScreenRequest request, UserPrincipal currentUser);

    ScreenResponse getScreenById(UUID screenId);

    List<ScreenResponse> getScreensByTheater(UUID theaterId);

    void deleteScreen(UUID screenId, UserPrincipal currentUser);
}
