package com.pravesh.controller;

import com.pravesh.dto.request.CreateSosRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.SosAlertResponse;
import com.pravesh.dto.response.SosStatusHistoryResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.SosService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<SosAlertResponse>> raise(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreateSosRequest req) {
        SosAlertResponse response = sosService.raise(req, caller.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("SOS alert raised", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('GUARD') or hasRole('SOCIETY_ADMIN')")
    public ResponseEntity<ApiResponse<List<SosAlertResponse>>> active(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Active alerts", sosService.getActiveForSociety(caller.societyId())));
    }

    // Full incident log, including RESOLVED alerts.
    @GetMapping("/log")
    @PreAuthorize("hasRole('GUARD') or hasRole('SOCIETY_ADMIN')")
    public ResponseEntity<ApiResponse<List<SosAlertResponse>>> incidentLog(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Incident log", sosService.getIncidentLog(caller.societyId())));
    }

    // A resident's own alerts -- page-load fallback for the live WebSocket push.
    @GetMapping("/mine")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<List<SosAlertResponse>>> mine(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("My alerts", sosService.getMyAlerts(caller.userId())));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('GUARD') or hasRole('SOCIETY_ADMIN')")
    public ResponseEntity<ApiResponse<SosAlertResponse>> updateStatus(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok("Status updated", sosService.updateStatus(id, body.get("status"), caller.userId())));
    }

    // Access is enforced inside the service (owner or same-society responder).
    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<SosStatusHistoryResponse>>> history(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Status history",
                sosService.getHistory(id, caller.userId(), caller.role(), caller.societyId())));
    }
}
