package com.pravesh.repository;

import com.pravesh.entity.enums.RequestStatus;
import com.pravesh.entity.ResidentRelocationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResidentRelocationRequestRepository extends JpaRepository<ResidentRelocationRequest, Long> {

    List<ResidentRelocationRequest> findByTargetSocietyIdAndStatusOrderByCreatedAtDesc(
            Long targetSocietyId, RequestStatus status);

    Optional<ResidentRelocationRequest> findByResident_UserIdAndStatus(
            Long residentUserId, RequestStatus status);
}
