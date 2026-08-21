package com.pravesh.controller;

import com.pravesh.dto.request.CreatePassRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.PassResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.PassService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RESIDENT')")
public class PassController {

    private final PassService passService;

    @PostMapping
    public ResponseEntity<ApiResponse<PassResponse>> createPass(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @Valid @RequestBody CreatePassRequest req) {
        PassResponse response = passService.createPass(caller.userId(), caller.societyId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Pass created", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PassResponse>>> myActivePasses(
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Active passes",
                passService.getMyActivePasses(caller.userId(), caller.societyId())));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<List<PassResponse>>> myHistory(
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Pass history",
                passService.getMyPassHistory(caller.userId(), caller.societyId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PassResponse>> passDetail(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Pass detail", passService.getPassDetail(id, caller.userId())));
    }

    @GetMapping("/{id}/qr")
    public ResponseEntity<ApiResponse<Map<String, String>>> regenerateQr(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("QR regenerated",
                Map.of("qrBase64", passService.regenerateQr(id, caller.userId()))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> revokePass(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id) {
        passService.revokePass(id, caller.userId());
        return ResponseEntity.ok(ApiResponse.ok("Pass revoked"));
    }

    @GetMapping("/admin/passes")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    public ResponseEntity<ApiResponse<List<PassResponse>>> allPassesInSociety(
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("All passes in society",
                passService.getAllPassesInSociety(caller.societyId())));
    }
}
