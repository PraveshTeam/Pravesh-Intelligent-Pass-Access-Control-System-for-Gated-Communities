package com.pravesh.service;

import com.pravesh.dto.response.*;
import com.pravesh.repository.EntryLogAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final EntryLogAnalyticsRepository repository;

    public SummaryResponse getTodaySummary(Long societyId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        return new SummaryResponse(
                repository.countTotalEntries(societyId, start, end),
                repository.countGranted(societyId, start, end),
                repository.countDenied(societyId, start, end),
                repository.countUniqueVisitors(societyId, start, end)
        );
    }

    public List<HourlyCountResponse> getHourlyHeatmap(Long societyId) {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        return repository.hourlyHeatmap(societyId, start).stream()
                .map(row -> new HourlyCountResponse(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).longValue()))
                .toList();
    }

    public List<DenyReasonCountResponse> getDeniedBreakdown(Long societyId) {
        return repository.deniedBreakdown(societyId).stream()
                .map(row -> new DenyReasonCountResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue()))
                .toList();
    }

    public List<VisitorCountResponse> getFrequentVisitors(Long societyId) {
        LocalDateTime start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        return repository.frequentVisitors(societyId, start).stream()
                .map(row -> new VisitorCountResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue()))
                .toList();
    }

    public List<GateCountResponse> getGateStats(Long societyId) {
        return repository.gateStats(societyId).stream()
                .map(row -> new GateCountResponse(
                        row[0] == null ? null : ((Number) row[0]).longValue(),
                        ((Number) row[1]).longValue()))
                .toList();
    }

    public List<DailyCountResponse> getWeeklyTrend(Long societyId) {
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        return repository.weeklyTrend(societyId, start).stream()
                .map(row -> new DailyCountResponse(
                        row[0].toString(),
                        ((Number) row[1]).longValue()))
                .toList();
    }
}