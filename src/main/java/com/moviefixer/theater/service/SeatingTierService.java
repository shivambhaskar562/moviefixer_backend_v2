package com.moviefixer.theater.service;

import com.moviefixer.theater.dto.SeatingTierRequest;
import com.moviefixer.theater.dto.SeatingTierResponse;

import java.util.List;
import java.util.UUID;

public interface SeatingTierService {

    List<SeatingTierResponse> getAllActiveTiers();

    SeatingTierResponse getTierById(UUID id);

    SeatingTierResponse createTier(SeatingTierRequest request);
}
