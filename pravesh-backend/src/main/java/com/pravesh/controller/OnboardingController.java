package com.pravesh.controller;

import com.pravesh.dto.request.ApproveRejectRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.OnboardingRequestResponse;
import com.pravesh.entity.enums.DocumentType;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.OnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OnboardingController {

	private final OnboardingService onboardingService;

	@PostMapping(value = "/api/onboarding/request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('RESIDENT')")
	public ResponseEntity<ApiResponse<OnboardingRequestResponse>> submitRequest(@AuthenticationPrincipal AuthenticatedUser caller,
			@RequestParam Long societyId, @RequestParam String claimedFlatNumber,
			@RequestParam(required = false) String tower, @RequestParam DocumentType documentType,
			@RequestParam("documentFile") MultipartFile documentFile) {

		var result = onboardingService.submitRequest(caller.userId(), societyId, claimedFlatNumber, tower, documentType,
				documentFile);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok("Onboarding request submitted. Awaiting admin review.", result));
	}

	@GetMapping("/api/onboarding/my-request")
	@PreAuthorize("hasRole('RESIDENT')")
	public ResponseEntity<ApiResponse<OnboardingRequestResponse>> myRequest(@AuthenticationPrincipal AuthenticatedUser caller) {
		return ResponseEntity.ok(ApiResponse.ok("Latest onboarding request", onboardingService.getMyLatestRequest(caller.userId())));
	}

	@GetMapping("/api/admin/onboarding/requests")
	@PreAuthorize("hasRole('SOCIETY_ADMIN')")
	public ResponseEntity<ApiResponse<List<OnboardingRequestResponse>>> listRequests(@AuthenticationPrincipal AuthenticatedUser caller,
			@RequestParam(defaultValue = "PENDING") String status) {
		var results = onboardingService.listByStatus(
				com.pravesh.entity.enums.RequestStatus.valueOf(status.toUpperCase()), caller.societyId());
		return ResponseEntity.ok(ApiResponse.ok("Onboarding requests", results));
	}

	@GetMapping("/api/admin/onboarding/requests/{id}/document")
	@PreAuthorize("hasAnyRole('SOCIETY_ADMIN', 'RESIDENT')")
	public ResponseEntity<Resource> downloadDocument(@AuthenticationPrincipal AuthenticatedUser caller,
			@PathVariable Long id) {

		Resource resource = onboardingService.getDocumentForDownload(id, caller.userId(), caller.role(),
				caller.societyId());

		String filename = resource.getFilename() != null ? resource.getFilename() : "document";
		MediaType contentType = com.pravesh.util.FileTypeUtil.detect(filename);

		return ResponseEntity.ok().contentType(contentType)
				.header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + filename + "\"")
				.body(resource);
	}

	// force defaults to false -- a normal approve() call from the UI never
	// sends it, so nothing changes for the happy path. Only the "Reassign
	// Anyway" confirm button in the conflict modal sends force=true.
	@PutMapping("/api/admin/onboarding/requests/{id}/approve")
	@PreAuthorize("hasRole('SOCIETY_ADMIN')")
	public ResponseEntity<ApiResponse<OnboardingRequestResponse>> approve(@AuthenticationPrincipal AuthenticatedUser caller,
			@PathVariable Long id, @RequestParam(defaultValue = "false") boolean force) {
		return ResponseEntity.ok(ApiResponse.ok("Request approved",
				onboardingService.approve(id, caller.userId(), caller.societyId(), force)));
	}

	@PutMapping("/api/admin/onboarding/requests/{id}/reject")
	@PreAuthorize("hasRole('SOCIETY_ADMIN')")
	public ResponseEntity<ApiResponse<OnboardingRequestResponse>> reject(@AuthenticationPrincipal AuthenticatedUser caller,
			@PathVariable Long id, @RequestBody ApproveRejectRequest req) {
		return ResponseEntity.ok(ApiResponse.ok("Request rejected",
				onboardingService.reject(id, caller.userId(), req.reason(), caller.societyId())));
	}
}
