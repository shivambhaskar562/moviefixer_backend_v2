package com.moviefixer.location.repository;

import com.moviefixer.location.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CityRepository extends JpaRepository<City, UUID> {

    List<City> findByStateId(UUID stateId);

    Optional<City> findByStateIdAndName(UUID stateId, String name);

    boolean existsByStateIdAndName(UUID stateId, String name);

    @Query("SELECT c FROM City c JOIN FETCH c.state s JOIN FETCH s.country")
    List<City> findAllWithStateAndCountry();
}
