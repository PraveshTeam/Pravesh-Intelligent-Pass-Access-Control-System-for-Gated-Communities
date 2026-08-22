package com.pravesh.repository;

import com.pravesh.entity.SosAlert;
import com.pravesh.entity.SosStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SosAlertRepository extends JpaRepository<SosAlert, Long> {

    List<SosAlert> findBySocietyIdAndStatusNotOrderByCreatedAtDesc(Long societyId, SosStatus excludeStatus);

    List<SosAlert> findByResident_UserIdOrderByCreatedAtDesc(Long residentUserId);

    // Full incident log, including RESOLVED alerts (the live-banner view excludes them).
    List<SosAlert> findBySocietyIdOrderByCreatedAtDesc(Long societyId);
}
