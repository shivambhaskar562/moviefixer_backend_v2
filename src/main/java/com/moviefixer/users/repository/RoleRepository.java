package com.moviefixer.users.repository;

import com.moviefixer.users.entity.Role;
import com.moviefixer.users.enums.USER_ROLE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(USER_ROLE name);

    boolean existsByName(USER_ROLE name);
}
