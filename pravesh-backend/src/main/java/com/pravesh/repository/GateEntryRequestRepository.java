package com.pravesh.repository;

import com.pravesh.entity.GateEntryRequest;
import com.pravesh.entity.GateRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GateEntryRequestRepository extends JpaRepository<GateEntryRequest, Long> {

    Optional<GateEntryRequest> findByIdAndGuardUserId(Long id, Long guardUserId);

    List<GateEntryRequest> findByResidentIdAndStatusOrderByCreatedAtDesc(Long residentId, GateRequestStatus status);

    List<GateEntryRequest> findByStatusAndExpiresAtBefore(GateRequestStatus status, LocalDateTime now);
}