package com.pravesh.repository;

import com.pravesh.entity.TripComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TripCommentRepository extends JpaRepository<TripComment, Long> {
    List<TripComment> findByTripIdOrderByCreatedAtAsc(Long tripId);
}
