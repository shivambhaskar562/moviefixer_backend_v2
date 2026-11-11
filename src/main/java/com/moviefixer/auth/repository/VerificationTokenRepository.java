package com.moviefixer.auth.repository;

import com.moviefixer.auth.entity.VerificationToken;
import com.moviefixer.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, UUID> {

    Optional<VerificationToken> findByToken(String token);

    Optional<VerificationToken> findTopByUserOrderByCreatedAtDesc(Users user);

    List<VerificationToken> findAllByUser(Users user);

    @Modifying
    void deleteByUser(Users user);
}
