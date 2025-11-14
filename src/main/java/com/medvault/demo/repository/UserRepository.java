package com.medvault.demo.repository;

import com.medvault.demo.entity.Role;
import com.medvault.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    // New method to find admin
    Optional<User> findByRole(Role role);

    // New method to check if admin exists
    boolean existsByRole(Role role);
}