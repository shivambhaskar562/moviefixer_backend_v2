package com.moviefixer.theater.repository;

import com.moviefixer.theater.entity.Theater;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TheaterRepository extends JpaRepository<Theater, UUID> {

    @Query("SELECT t FROM Theater t " +
            "JOIN FETCH t.city c " +
            "JOIN FETCH c.state s " +
            "JOIN FETCH s.country " +
            "JOIN FETCH t.vendor v " +
            "WHERE t.id = :id")
    Optional<Theater> findByIdWithDetails(@Param("id") UUID id);

    Page<Theater> findByCityIdAndActiveTrue(UUID cityId, Pageable pageable);

    Page<Theater> findByVendorId(UUID vendorId, Pageable pageable);

    Optional<Theater> findByIdAndVendorId(UUID id, UUID vendorId);

    boolean existsByNameIgnoreCaseAndCityId(String name, UUID cityId);

    @Query("SELECT t FROM Theater t " +
            "JOIN FETCH t.city c " +
            "WHERE (:cityId IS NULL OR c.id = :cityId) " +
            "AND (:keyword IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "     OR LOWER(t.address) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND t.active = true")
    Page<Theater> searchTheaters(
            @Param("cityId") UUID cityId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    long countByCityIdAndActiveTrue(UUID cityId);
}
