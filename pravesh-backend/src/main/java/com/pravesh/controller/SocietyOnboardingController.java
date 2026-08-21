package com.pravesh.controller;

import com.pravesh.dto.request.ApproveRejectRequest;
import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.SocietyRegistrationResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.SocietyAdminOnboardingService;
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
public class SocietyOnboardingController {

	private final SocietyAdminOnboardingService onboardingService;

	@PostMapping(value = "/api/society-onboarding/request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('SOCIETY_ADMIN')")
	public ResponseEntity<ApiResponse<SocietyRegistrationResponse>> submitRequest(@AuthenticationPrincipal AuthenticatedUser caller,
			@RequestParam String societyName, @RequestParam(required = false) String address,
			@RequestParam(required = false) String city, @RequestParam("documentFile") MultipartFile documentFile) {

		var result = onboardingService.submitRequest(caller.userId(), societyName, address, city, documentFile);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.ok("Society registration request submitted. Awaiting super admin review.", result));
	}

	@GetMapping("/api/society-onboarding/my-request")
	@PreAuthorize("hasRole('SOCIETY_ADMIN')")
	public ResponseEntity<ApiResponse<SocietyRegistrationResponse>> myRequest(@AuthenticationPrincipal AuthenticatedUser caller) {
		return ResponseEntity.ok(ApiResponse.ok("Latest request", onboardingService.getMyLatestRequest(caller.userId())));
	}

	@GetMapping("/api/superadmin/society-requests")
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	public ResponseEntity<ApiResponse<List<SocietyRegistrationResponse>>> listRequests(
			@RequestParam(defaultValue = "PENDING") String status) {
		var results = onboardingService
				.listByStatus(com.pravesh.entity.enums.RequestStatus.valueOf(status.toUpperCase()));
		return ResponseEntity.ok(ApiResponse.ok("Society registration requests", results));
	}

	@GetMapping("/api/superadmin/society-requests/{id}/document")
	@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SOCIETY_ADMIN')")
	public ResponseEntity<Resource> downloadDocument(@AuthenticationPrincipal AuthenticatedUser caller,
			@PathVariable Long id) {
		Resource resource = onboardingService.getDocumentForDownload(id, caller.userId(), caller.role());

		String filename = resource.getFilename() != null ? resource.getFilename() : "document";
		MediaType contentType = com.pravesh.util.FileTypeUtil.detect(filename);

		return ResponseEntity.ok().contentType(contentType)
				.header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + filename + "\"")
				.body(resource);
	}

	@PutMapping("/api/superadmin/society-requests/{id}/approve")
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	public ResponseEntity<ApiResponse<SocietyRegistrationResponse>> approve(@AuthenticationPrincipal AuthenticatedUser caller,
			@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.ok("Request approved", onboardingService.approve(id, caller.userId())));
	}

	@PutMapping("/api/superadmin/society-requests/{id}/reject")
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	public ResponseEntity<ApiResponse<SocietyRegistrationResponse>> reject(@AuthenticationPrincipal AuthenticatedUser caller,
			@PathVariable Long id, @RequestBody ApproveRejectRequest req) {
		return ResponseEntity.ok(ApiResponse.ok("Request rejected", onboardingService.reject(id, caller.userId(), req.reason())));
	}
}
