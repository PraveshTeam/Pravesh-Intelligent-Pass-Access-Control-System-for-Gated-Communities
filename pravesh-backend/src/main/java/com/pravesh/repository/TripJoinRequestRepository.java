package com.pravesh.repository;

import com.pravesh.entity.JoinRequestStatus;
import com.pravesh.entity.TripJoinRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TripJoinRequestRepository extends JpaRepository<TripJoinRequest, Long> {

    List<TripJoinRequest> findByTripIdOrderByCreatedAtAsc(Long tripId);

    List<TripJoinRequest> findByTripIdAndStatus(Long tripId, JoinRequestStatus status);

    Optional<TripJoinRequest> findByIdAndTripId(Long id, Long tripId);

    Optional<TripJoinRequest> findByTripIdAndRequesterId(Long tripId, Long requesterId);

    long countByTripIdAndStatus(Long tripId, JoinRequestStatus status);
}
