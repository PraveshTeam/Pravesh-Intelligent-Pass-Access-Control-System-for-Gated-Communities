package com.pravesh.repository;

import com.pravesh.entity.EntryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EntryLogAnalyticsRepository extends JpaRepository<EntryLog, Long> {

    @Query(value = "SELECT COUNT(*) FROM entry_logs " +
                   "WHERE society_id = :societyId AND scanned_at >= :start AND scanned_at < :end",
           nativeQuery = true)
    long countTotalEntries(@Param("societyId") Long societyId,
                           @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT COUNT(*) FROM entry_logs WHERE society_id = :societyId AND scan_result = 'GRANTED' " +
                   "AND scanned_at >= :start AND scanned_at < :end",
           nativeQuery = true)
    long countGranted(@Param("societyId") Long societyId,
                       @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT COUNT(*) FROM entry_logs WHERE society_id = :societyId AND scan_result = 'DENIED' " +
                   "AND scanned_at >= :start AND scanned_at < :end",
           nativeQuery = true)
    long countDenied(@Param("societyId") Long societyId,
                      @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT COUNT(DISTINCT visitor_name) FROM entry_logs " +
                   "WHERE society_id = :societyId AND scanned_at >= :start AND scanned_at < :end",
           nativeQuery = true)
    long countUniqueVisitors(@Param("societyId") Long societyId,
                              @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT HOUR(scanned_at) AS hour, COUNT(*) AS cnt " +
                   "FROM entry_logs WHERE society_id = :societyId AND scanned_at >= :start " +
                   "GROUP BY HOUR(scanned_at) ORDER BY hour",
           nativeQuery = true)
    List<Object[]> hourlyHeatmap(@Param("societyId") Long societyId, @Param("start") LocalDateTime start);

    @Query(value = "SELECT deny_reason, COUNT(*) AS cnt FROM entry_logs " +
                   "WHERE society_id = :societyId AND scan_result = 'DENIED' AND deny_reason IS NOT NULL " +
                   "GROUP BY deny_reason ORDER BY cnt DESC",
           nativeQuery = true)
    List<Object[]> deniedBreakdown(@Param("societyId") Long societyId);

    @Query(value = "SELECT visitor_name, COUNT(*) AS cnt FROM entry_logs " +
                   "WHERE society_id = :societyId AND scanned_at >= :start " +
                   "GROUP BY visitor_name ORDER BY cnt DESC LIMIT 10",
           nativeQuery = true)
    List<Object[]> frequentVisitors(@Param("societyId") Long societyId, @Param("start") LocalDateTime start);

    @Query(value = "SELECT gate_id, COUNT(*) AS cnt FROM entry_logs " +
                   "WHERE society_id = :societyId " +
                   "GROUP BY gate_id ORDER BY cnt DESC",
           nativeQuery = true)
    List<Object[]> gateStats(@Param("societyId") Long societyId);

    @Query(value = "SELECT DATE(scanned_at) AS day, COUNT(*) AS cnt FROM entry_logs " +
                   "WHERE society_id = :societyId AND scanned_at >= :start " +
                   "GROUP BY DATE(scanned_at) ORDER BY day DESC LIMIT 30",
           nativeQuery = true)
    List<Object[]> weeklyTrend(@Param("societyId") Long societyId, @Param("start") LocalDateTime start);
}