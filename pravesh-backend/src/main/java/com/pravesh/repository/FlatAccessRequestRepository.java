package com.pravesh.repository;

import com.pravesh.entity.FlatAccessRequest;
import com.pravesh.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlatAccessRequestRepository extends JpaRepository<FlatAccessRequest, Long> {

    List<FlatAccessRequest> findByStatus(RequestStatus status);

    Optional<FlatAccessRequest> findTopByUser_IdOrderByCreatedAtDesc(Long userId);

    boolean existsByUser_IdAndStatus(Long userId, RequestStatus status);

    List<FlatAccessRequest> findByStatusAndSocietyId(RequestStatus status, Long societyId);
}
