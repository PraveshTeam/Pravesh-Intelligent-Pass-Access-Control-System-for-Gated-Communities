package com.pravesh.controller;

import com.pravesh.dto.request.CreateGuardRequest;
import com.pravesh.dto.request.ReassignGateRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.GuardResponse;
import com.pravesh.dto.response.GuardShiftResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.GuardManagementService;
import com.pravesh.service.ShiftCheckinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/guards")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SOCIETY_ADMIN')")
public class GuardAdminController {

    private final GuardManagementService guardManagementService;
    private final ShiftCheckinService shiftCheckinService;

    @PostMapping
    public ResponseEntity<ApiResponse<GuardResponse>> createGuard(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreateGuardRequest req) {
        GuardResponse response = guardManagementService.createGuard(req, caller.userId(), caller.societyId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Guard account created. Credentials sent via SMS.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GuardResponse>>> listGuards(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Guard accounts", guardManagementService.listGuards(caller.societyId())));
    }

    @PutMapping("/{id}/reassign-gate")
    public ResponseEntity<ApiResponse<GuardResponse>> reassignGate(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id,
            @Valid @RequestBody ReassignGateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Guard reassigned",
                guardManagementService.reassignGate(id, req, caller.societyId())));
    }

    @GetMapping("/{id}/shifts")
    public ResponseEntity<ApiResponse<List<GuardShiftResponse>>> shiftHistory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Shift history", shiftCheckinService.getShiftHistory(id)));
    }
}
