package com.moviefixer.theater.service;

import com.moviefixer.auth.security.UserPrincipal;
import com.moviefixer.theater.dto.CreateTheaterRequest;
import com.moviefixer.theater.dto.TheaterResponse;
import com.moviefixer.theater.dto.TheaterSummaryResponse;
import com.moviefixer.theater.dto.UpdateTheaterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TheaterService {

    TheaterResponse createTheater(CreateTheaterRequest request, UserPrincipal currentUser);

    TheaterResponse updateTheater(UUID theaterId, UpdateTheaterRequest request, UserPrincipal currentUser);

    TheaterResponse getTheaterById(UUID theaterId);

    Page<TheaterSummaryResponse> searchTheaters(UUID cityId, String keyword, Pageable pageable);

    Page<TheaterResponse> getTheatersByVendor(UUID vendorId, Pageable pageable, UserPrincipal currentUser);

    void deleteTheater(UUID theaterId, UserPrincipal currentUser);
}
