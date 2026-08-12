package com.pravesh.repository;

import com.pravesh.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    // Every trip listing/lookup is society-scoped -- built in from the start
    // this time, learned from the payment/forum cross-tenant leak fixes.
    List<Trip> findBySocietyIdOrderByCreatedAtDesc(Long societyId);

    Optional<Trip> findByIdAndSocietyId(Long id, Long societyId);
}
