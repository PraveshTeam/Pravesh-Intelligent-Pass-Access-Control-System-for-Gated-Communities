package com.pravesh.controller;

import com.pravesh.dto.request.CreateSosRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.SosAlertResponse;
import com.pravesh.dto.response.SosStatusHistoryResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.SosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sos")
@RequiredArgsConstructor
public class SosController {

    private final SosService sosService;

    @PostMapping
    @PreAuthorize("hasRole('RESIDENT')")
    public ApiResponse<SosAlertResponse> raise(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreateSosRequest req) {
        return ApiResponse.ok("SOS alert raised", sosService.raise(req, caller.userId()));
    }

    @GetMapping
    @PreAuthorize("hasRole('GUARD') or hasRole('SOCIETY_ADMIN')")
    public ApiResponse<List<SosAlertResponse>> active(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Active alerts", sosService.getActiveForSociety(caller.societyId()));
    }

    // New: full incident log, including RESOLVED alerts -- for reviewing past
    // incidents rather than just what's currently live. Same access level as
    // the active-alerts view (GUARD or SOCIETY_ADMIN of this society).
    @GetMapping("/log")
    @PreAuthorize("hasRole('GUARD') or hasRole('SOCIETY_ADMIN')")
    public ApiResponse<List<SosAlertResponse>> incidentLog(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Incident log", sosService.getIncidentLog(caller.societyId()));
    }

    // New: lets a resident see the live status of their OWN alerts. Powers
    // the resident-facing status banner -- fetched on page load as a
    // reliable fallback, on top of the live WebSocket push.
    @GetMapping("/mine")
    @PreAuthorize("hasRole('RESIDENT')")
    public ApiResponse<List<SosAlertResponse>> mine(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("My alerts", sosService.getMyAlerts(caller.userId()));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('GUARD') or hasRole('SOCIETY_ADMIN')")
    public ApiResponse<SosAlertResponse> updateStatus(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        return ApiResponse.ok("Status updated", sosService.updateStatus(id, body.get("status"), caller.userId()));
    }

    // New: full acknowledgment/progress/resolve timeline for one alert.
    // Access is enforced inside the service (owner or same-society responder
    // only) rather than at the @PreAuthorize level, since "same society" and
    // "is the owner" both need the actual alert row to check against.
    @GetMapping("/{id}/history")
    public ApiResponse<List<SosStatusHistoryResponse>> history(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id) {
        return ApiResponse.ok("Status history",
                sosService.getHistory(id, caller.userId(), caller.role(), caller.societyId()));
    }
}
