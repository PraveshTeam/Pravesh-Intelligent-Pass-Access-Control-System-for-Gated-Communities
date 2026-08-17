package com.pravesh.controller;

import com.pravesh.dto.request.CreateRelocationRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.RelocationRequestResponse;
import com.pravesh.entity.enums.RequestStatus;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.ResidentRelocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResidentRelocationController {

    private final ResidentRelocationService service;

    @PostMapping(value = "/api/relocation/request", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<RelocationRequestResponse>> create(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam Long targetSocietyId,
            @RequestParam String claimedFlatNumber,
            @RequestParam(required = false) String tower,
            @RequestParam String documentType,
            @RequestParam("documentFile") MultipartFile documentFile) {

        var req = new CreateRelocationRequest(targetSocietyId, claimedFlatNumber, tower, documentType);
        RelocationRequestResponse response = service.createRequest(req, caller.userId(), documentFile);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Relocation request submitted", response));
    }

    @GetMapping("/api/admin/relocation-requests")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    public ResponseEntity<ApiResponse<List<RelocationRequestResponse>>> list(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam(defaultValue = "PENDING") RequestStatus status) {
        return ResponseEntity.ok(ApiResponse.ok("Relocation requests",
                service.getRequestsForSociety(caller.societyId(), status)));
    }

    @PutMapping("/api/admin/relocation-requests/{id}/approve")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    public ResponseEntity<ApiResponse<RelocationRequestResponse>> approve(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force) {
        return ResponseEntity.ok(ApiResponse.ok("Approved", service.approve(id, caller.userId(), force)));
    }

    @PutMapping("/api/admin/relocation-requests/{id}/reject")
    @PreAuthorize("hasRole('SOCIETY_ADMIN')")
    public ResponseEntity<ApiResponse<RelocationRequestResponse>> reject(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok("Rejected", service.reject(id, caller.userId(), body.get("reason"))));
    }

    @GetMapping("/api/relocation/my-request")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<RelocationRequestResponse>> myRequest(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Current request", service.getMyPendingRequest(caller.userId())));
    }

    @DeleteMapping("/api/relocation/{id}")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Void>> revoke(@AuthenticationPrincipal AuthenticatedUser caller, @PathVariable Long id) {
        service.revoke(id, caller.userId());
        return ResponseEntity.ok(ApiResponse.ok("Request revoked"));
    }

    @GetMapping("/api/admin/relocation-requests/{id}/document")
    @PreAuthorize("hasAnyRole('SOCIETY_ADMIN', 'RESIDENT')")
    public org.springframework.http.ResponseEntity<org.springframework.core.io.Resource> downloadDocument(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @PathVariable Long id) {

        org.springframework.core.io.Resource resource = service.getDocumentForDownload(
                id, caller.userId(), caller.role(), caller.societyId());

        String filename = resource.getFilename() != null ? resource.getFilename() : "document";
        org.springframework.http.MediaType contentType = com.pravesh.util.FileTypeUtil.detect(filename);

        return org.springframework.http.ResponseEntity.ok()
                .contentType(contentType)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
