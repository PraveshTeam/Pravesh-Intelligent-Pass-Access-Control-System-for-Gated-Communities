package com.pravesh.repository;

import com.pravesh.entity.Gate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GateRepository extends JpaRepository<Gate, Long> {
    List<Gate> findBySocietyId(Long societyId);
}