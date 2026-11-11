package com.moviefixer.theater.repository;

import com.moviefixer.theater.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScreenRepository extends JpaRepository<Screen, UUID> {

    @Query("SELECT s FROM Screen s JOIN FETCH s.theater t WHERE s.id = :id")
    Optional<Screen> findByIdWithTheater(@Param("id") UUID id);

    List<Screen> findByTheaterIdAndActiveTrue(UUID theaterId);

    List<Screen> findByTheaterId(UUID theaterId);

    Optional<Screen> findByIdAndTheaterId(UUID id, UUID theaterId);

    boolean existsByTheaterIdAndNameIgnoreCase(UUID theaterId, String name);

    long countByTheaterIdAndActiveTrue(UUID theaterId);
}
