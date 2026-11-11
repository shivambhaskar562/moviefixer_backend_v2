package com.moviefixer.theater.service;

import com.moviefixer.common.exception.ResourceNotFoundException;
import com.moviefixer.theater.dto.SeatingTierRequest;
import com.moviefixer.theater.dto.SeatingTierResponse;
import com.moviefixer.theater.entity.SeatingTier;
import com.moviefixer.theater.repository.SeatingTierRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SeatingTierServiceImpl implements SeatingTierService {

    private static final Logger log = LoggerFactory.getLogger(SeatingTierServiceImpl.class);

    private final SeatingTierRepository seatingTierRepository;

    public SeatingTierServiceImpl(SeatingTierRepository seatingTierRepository) {
        this.seatingTierRepository = seatingTierRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatingTierResponse> getAllActiveTiers() {
        return seatingTierRepository.findByActiveTrueOrderByDefaultMultiplierDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SeatingTierResponse getTierById(UUID id) {
        SeatingTier tier = seatingTierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SeatingTier", "id", id));
        return mapToResponse(tier);
    }

    @Override
    @Transactional
    public SeatingTierResponse createTier(SeatingTierRequest request) {
        if (seatingTierRepository.existsByTierType(request.getTierType())) {
            throw new IllegalArgumentException("Seating tier with type " + request.getTierType() + " already exists");
        }
        if (seatingTierRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new IllegalArgumentException("Seating tier with name '" + request.getName() + "' already exists");
        }

        SeatingTier tier = new SeatingTier(
                request.getName().trim(),
                request.getTierType(),
                request.getDefaultMultiplier(),
                request.getDescription(),
                (request.getActive() != null) ? request.getActive() : true
        );

        SeatingTier saved = seatingTierRepository.save(tier);
        log.info("Created seating tier: {} [Type: {}] [Multiplier: {}x]", saved.getName(), saved.getTierType(), saved.getDefaultMultiplier());
        return mapToResponse(saved);
    }

    private SeatingTierResponse mapToResponse(SeatingTier tier) {
        return new SeatingTierResponse(
                tier.getId(),
                tier.getName(),
                tier.getTierType(),
                tier.getDefaultMultiplier(),
                tier.getDescription(),
                tier.isActive()
        );
    }
}
