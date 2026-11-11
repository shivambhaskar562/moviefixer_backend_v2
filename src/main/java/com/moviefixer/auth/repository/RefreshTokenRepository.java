package com.moviefixer.auth.repository;

import com.moviefixer.auth.entity.RefreshToken;
import com.moviefixer.users.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUser(Users user);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true, r.revokedAt = :now WHERE r.user = :user AND r.revoked = false")
    void revokeAllActiveByUser(@Param("user") Users user, @Param("now") Instant now);

    default void revokeAllActiveByUser(Users user) {
        revokeAllActiveByUser(user, Instant.now());
    }

    @Modifying
    void deleteByUser(Users user);
}
