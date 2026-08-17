package com.pravesh.controller;

import com.pravesh.dto.response.*;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SOCIETY_ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<SummaryResponse>> summary(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Today's summary", analyticsService.getTodaySummary(caller.societyId())));
    }

    @GetMapping("/hourly")
    public ResponseEntity<ApiResponse<List<HourlyCountResponse>>> hourly(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Hourly heatmap (last 7 days)", analyticsService.getHourlyHeatmap(caller.societyId())));
    }

    @GetMapping("/denied-breakdown")
    public ResponseEntity<ApiResponse<List<DenyReasonCountResponse>>> deniedBreakdown(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Denied entries by reason", analyticsService.getDeniedBreakdown(caller.societyId())));
    }

    @GetMapping("/frequent-visitors")
    public ResponseEntity<ApiResponse<List<VisitorCountResponse>>> frequentVisitors(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Top visitors this month", analyticsService.getFrequentVisitors(caller.societyId())));
    }

    @GetMapping("/gate-stats")
    public ResponseEntity<ApiResponse<List<GateCountResponse>>> gateStats(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Entries per gate", analyticsService.getGateStats(caller.societyId())));
    }

    @GetMapping("/weekly-trend")
    public ResponseEntity<ApiResponse<List<DailyCountResponse>>> weeklyTrend(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Daily entries (last 30 days)", analyticsService.getWeeklyTrend(caller.societyId())));
    }
}