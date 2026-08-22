package com.pravesh.repository;

import com.pravesh.entity.EntryLog;
import com.pravesh.entity.enums.ScanResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EntryLogRepository extends JpaRepository<EntryLog, Long> {

    List<EntryLog> findByGateIdAndScannedAtBetween(
            Long gateId, LocalDateTime start, LocalDateTime end);

    List<EntryLog> findByScanResult(ScanResult scanResult);

    List<EntryLog> findByResident_UserId(Long residentId);

    long countByScannedAtBetween(LocalDateTime start, LocalDateTime end);

    List<EntryLog> findByGateIdAndScannedAtBetweenAndSocietyId(
            Long gateId, LocalDateTime start, LocalDateTime end, Long societyId);

    List<EntryLog> findByResident_UserIdAndSocietyId(Long residentId, Long societyId);

    List<EntryLog> findBySocietyId(Long societyId);
}
