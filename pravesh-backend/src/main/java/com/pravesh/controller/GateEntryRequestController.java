package com.pravesh.controller;

import com.pravesh.dto.request.CreateGateEntryRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.GateEntryRequestResponse;
import com.pravesh.dto.response.ResidentDirectoryEntry;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.GateEntryRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gate-requests")
@RequiredArgsConstructor
public class GateEntryRequestController {

    private final GateEntryRequestService service;

    @PostMapping
    @PreAuthorize("hasRole('GUARD')")
    public ApiResponse<GateEntryRequestResponse> create(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreateGateEntryRequest req) {
        return ApiResponse.ok("Request sent to resident",
                service.createRequest(req, caller.userId(), caller.societyId()));
    }

    @GetMapping("/{id}/status")
    @PreAuthorize("hasRole('GUARD')")
    public ApiResponse<GateEntryRequestResponse> status(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id) {
        return ApiResponse.ok("Status", service.getStatus(id, caller.userId()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('RESIDENT')")
    public ApiResponse<List<GateEntryRequestResponse>> pending(
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Pending requests", service.getMyPendingRequests(caller.userId()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('RESIDENT')")
    public ApiResponse<GateEntryRequestResponse> approve(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id) {
        return ApiResponse.ok("Approved", service.respond(id, caller.userId(), true));
    }

    @PutMapping("/{id}/deny")
    @PreAuthorize("hasRole('RESIDENT')")
    public ApiResponse<GateEntryRequestResponse> deny(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id) {
        return ApiResponse.ok("Denied", service.respond(id, caller.userId(), false));
    }
    
    @GetMapping("/residents")
    @PreAuthorize("hasRole('GUARD')")
    public ApiResponse<List<ResidentDirectoryEntry>> residents(
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Residents", service.getSocietyResidents(caller.societyId()));
    }
}