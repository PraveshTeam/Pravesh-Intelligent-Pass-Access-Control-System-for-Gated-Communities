package com.pravesh.repository;

import com.pravesh.entity.Flat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FlatRepository extends JpaRepository<Flat, Long> {
    Optional<Flat> findBySocietyIdAndFlatNumber(Long societyId, String flatNumber);
    List<Flat> findBySocietyId(Long societyId);
}