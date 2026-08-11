package com.pravesh.controller;

import com.pravesh.dto.request.ScanRequest;
import com.pravesh.dto.request.WalkInEntryLogRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.EntryLogResponse;
import com.pravesh.dto.response.ScanResultResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.ValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ValidationController {

    private final ValidationService validationService;

    @PostMapping("/api/validate/scan")
    @PreAuthorize("hasRole('GUARD')")
    public ApiResponse<ScanResultResponse> scan(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam Long gateId,
            @Valid @RequestBody ScanRequest req) {

        var result = validationService.scan(req.uuid(), caller.userId(), gateId, caller.societyId());

        return ApiResponse.ok(
                result.granted() ? "Entry granted" : "Entry denied",
                new ScanResultResponse(result.granted(), result.reason(),
                        result.visitorName(), result.passType()));
    }

    @PostMapping("/api/internal/entries/walk-in")
    public ApiResponse<Void> logWalkInEntry(@RequestBody WalkInEntryLogRequest req) {
        validationService.logWalkInEntry(req);
        return ApiResponse.ok("Walk-in entry logged", null);
    }

    @GetMapping("/api/entries")
    @PreAuthorize("hasRole('GUARD')")
    public ApiResponse<List<EntryLogResponse>> myGateEntries(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam Long gateId,
            @RequestParam(required = false) String date) {

        LocalDate d = date != null ? LocalDate.parse(date) : LocalDate.now();
        var entries = validationService.getEntriesByGate(gateId, d, caller.societyId()).stream()
                .map(e -> new EntryLogResponse(e.getId(), e.getVisitorName(), e.getResidentId(),
                        e.getEntryType().name(), e.getScanResult().name(), e.getDenyReason(), e.getScannedAt()))
                .toList();

        return ApiResponse.ok("Entry log", entries);
    }

    @GetMapping("/api/entries/flat/{flatId}")
    @PreAuthorize("hasRole('RESIDENT')")
    public ApiResponse<List<EntryLogResponse>> flatEntries(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long flatId) {

        var entries = validationService.getEntriesByFlat(caller.userId(), caller.societyId()).stream()
                .map(e -> new EntryLogResponse(e.getId(), e.getVisitorName(), e.getResidentId(),
                        e.getEntryType().name(), e.getScanResult().name(), e.getDenyReason(), e.getScannedAt()))
                .toList();

        return ApiResponse.ok("Flat entry log", entries);
    }

    @GetMapping("/api/admin/entries")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    public ApiResponse<List<EntryLogResponse>> allEntries(
            @AuthenticationPrincipal AuthenticatedUser caller) {
        var entries = validationService.getAllEntriesInSociety(caller.societyId()).stream()
                .map(e -> new EntryLogResponse(e.getId(), e.getVisitorName(), e.getResidentId(),
                        e.getEntryType().name(), e.getScanResult().name(), e.getDenyReason(), e.getScannedAt()))
                .toList();
        return ApiResponse.ok("All entries in society", entries);
    }
}