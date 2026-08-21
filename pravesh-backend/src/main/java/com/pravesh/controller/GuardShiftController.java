package com.pravesh.controller;

import com.pravesh.dto.request.ShiftCheckinRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.ShiftCheckinResponse;
import com.pravesh.entity.GuardShift;
import com.pravesh.repository.GuardShiftRepository;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.ShiftCheckinService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/guard")
@RequiredArgsConstructor
public class GuardShiftController {

    private final ShiftCheckinService shiftCheckinService;
    private final GuardShiftRepository guardShiftRepository;

    @PostMapping("/shift-checkin")
    @PreAuthorize("hasRole('GUARD')")
    public ResponseEntity<ApiResponse<ShiftCheckinResponse>> checkIn(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody ShiftCheckinRequest req) {
        ShiftCheckinResponse response = shiftCheckinService.checkIn(caller.userId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Shift started", response));
    }

    @PostMapping("/shift-checkout")
    @PreAuthorize("hasRole('GUARD')")
    public ResponseEntity<ApiResponse<Void>> checkOut(@AuthenticationPrincipal AuthenticatedUser caller) {
        shiftCheckinService.endShift(caller.userId());
        return ResponseEntity.ok(ApiResponse.ok("Shift ended"));
    }

    @GetMapping("/shift-status")
    public ResponseEntity<ApiResponse<ShiftStatusResponse>> getShiftStatus(
            @AuthenticationPrincipal AuthenticatedUser caller) {

        var activeShift = guardShiftRepository
                .findTopByGuardUserIdAndShiftEndIsNullOrderByShiftStartDesc(caller.userId());

        if (activeShift.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.ok("No active shift",
                    new ShiftStatusResponse(false, null, null, null)));
        }

        GuardShift shift = activeShift.get();
        return ResponseEntity.ok(ApiResponse.ok("Active shift found",
                new ShiftStatusResponse(true, shift.getId(), shift.getGateId(), shift.getOnDutyName())));
    }

    public record ShiftStatusResponse(
            boolean hasActiveShift,
            Long shiftId,
            Long gateId,
            String onDutyName
    ) {}
}
