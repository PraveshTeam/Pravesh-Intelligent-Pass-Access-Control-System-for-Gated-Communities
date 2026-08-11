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
    public ApiResponse<GuardResponse> createGuard(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreateGuardRequest req) {
        return ApiResponse.ok("Guard account created. Credentials sent via SMS.",
                guardManagementService.createGuard(req, caller.userId(), caller.societyId()));
    }

    @GetMapping
    public ApiResponse<List<GuardResponse>> listGuards(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("Guard accounts", guardManagementService.listGuards(caller.societyId()));
    }

    @PutMapping("/{id}/reassign-gate")
    public ApiResponse<GuardResponse> reassignGate(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id,
            @Valid @RequestBody ReassignGateRequest req) {
        return ApiResponse.ok("Guard reassigned",
                guardManagementService.reassignGate(id, req, caller.societyId()));
    }

    @GetMapping("/{id}/shifts")
    public ApiResponse<List<GuardShiftResponse>> shiftHistory(@PathVariable Long id) {
        return ApiResponse.ok("Shift history", shiftCheckinService.getShiftHistory(id));
    }
}