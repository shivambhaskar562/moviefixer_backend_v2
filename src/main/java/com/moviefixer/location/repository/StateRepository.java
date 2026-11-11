package com.moviefixer.location.repository;

import com.moviefixer.location.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StateRepository extends JpaRepository<State, UUID> {

    List<State> findByCountryId(UUID countryId);

    Optional<State> findByCountryIdAndName(UUID countryId, String name);

    boolean existsByCountryIdAndName(UUID countryId, String name);
}
