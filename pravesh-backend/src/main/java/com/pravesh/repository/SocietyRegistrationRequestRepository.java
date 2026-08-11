package com.pravesh.repository;

import com.pravesh.entity.SocietyRegistrationRequest;
import com.pravesh.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocietyRegistrationRequestRepository extends JpaRepository<SocietyRegistrationRequest, Long> {
    List<SocietyRegistrationRequest> findByStatus(RequestStatus status);
    Optional<SocietyRegistrationRequest> findTopByAdminUserIdOrderByCreatedAtDesc(Long adminUserId);
    boolean existsByAdminUserIdAndStatus(Long adminUserId, RequestStatus status);
}