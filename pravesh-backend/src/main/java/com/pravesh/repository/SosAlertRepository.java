package com.pravesh.repository;

import com.pravesh.entity.SosAlert;
import com.pravesh.entity.SosStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SosAlertRepository extends JpaRepository<SosAlert, Long> {
    List<SosAlert> findBySocietyIdAndStatusNotOrderByCreatedAtDesc(Long societyId, SosStatus excludeStatus);
    List<SosAlert> findByResidentUserIdOrderByCreatedAtDesc(Long residentUserId);

    // New: the full incident log -- every alert ever raised in this society,
    // including RESOLVED ones. getActiveForSociety() deliberately excludes
    // RESOLVED (that's the live-banner view); this is the separate "history"
    // view so an admin can review past incidents, not just what's happening now.
    List<SosAlert> findBySocietyIdOrderByCreatedAtDesc(Long societyId);
}
