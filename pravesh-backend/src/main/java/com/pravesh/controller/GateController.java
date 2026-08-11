package com.pravesh.controller;

import com.pravesh.dto.request.CreateGateRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.GateResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.GateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/gates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SOCIETY_ADMIN')")
public class GateController {

    private final GateService gateService;

    @PostMapping
    public ApiResponse<GateResponse> createGate(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreateGateRequest req) {
        return ApiResponse.ok("Gate created", gateService.createGate(req, caller.societyId()));
    }

    @GetMapping
    public ApiResponse<List<GateResponse>> listGates(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam(defaultValue = "false") boolean unassigned) {
        return ApiResponse.ok("Gates in your society", gateService.listGates(caller.societyId(), unassigned));
    }
}