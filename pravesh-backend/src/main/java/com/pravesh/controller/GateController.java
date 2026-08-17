package com.pravesh.controller;

import com.pravesh.dto.request.CreateGateRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.GateResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.GateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<GateResponse>> createGate(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreateGateRequest req) {
        GateResponse response = gateService.createGate(req, caller.societyId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Gate created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GateResponse>>> listGates(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam(defaultValue = "false") boolean unassigned) {
        return ResponseEntity.ok(ApiResponse.ok("Gates in your society", gateService.listGates(caller.societyId(), unassigned)));
    }
}
