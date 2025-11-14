package com.medvault.demo.repository;

import com.medvault.demo.entity.RegistrationRequest;
import com.medvault.demo.entity.RequestStatus;
import com.medvault.demo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {

    Optional<RegistrationRequest> findByEmail(String email);

    boolean existsByEmail(String email);

    List<RegistrationRequest> findByStatus(RequestStatus status);

    List<RegistrationRequest> findByRequestedRole(Role role);
}