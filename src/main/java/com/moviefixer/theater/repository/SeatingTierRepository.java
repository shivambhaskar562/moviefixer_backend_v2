package com.moviefixer.theater.repository;

import com.moviefixer.theater.entity.SeatingTier;
import com.moviefixer.theater.enums.TierType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SeatingTierRepository extends JpaRepository<SeatingTier, UUID> {

    List<SeatingTier> findByActiveTrueOrderByDefaultMultiplierDesc();

    Optional<SeatingTier> findByTierType(TierType tierType);

    boolean existsByTierType(TierType tierType);

    boolean existsByNameIgnoreCase(String name);
}
