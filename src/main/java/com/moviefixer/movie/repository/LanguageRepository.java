package com.moviefixer.movie.repository;

import com.moviefixer.movie.entity.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LanguageRepository extends JpaRepository<Language, UUID> {

    Optional<Language> findByCodeIgnoreCase(String code);

    Optional<Language> findByNameIgnoreCase(String name);

    List<Language> findByActiveTrueOrderByNameAsc();

    boolean existsByCodeIgnoreCase(String code);
}
