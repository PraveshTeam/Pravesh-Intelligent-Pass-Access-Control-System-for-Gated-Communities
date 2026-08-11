package com.pravesh.service;

import com.pravesh.dto.request.CreateRelocationRequest;
import com.pravesh.dto.response.RelocationRequestResponse;
import com.pravesh.entity.*;
import com.pravesh.entity.enums.*;
import com.pravesh.exception.*;
import com.pravesh.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class ResidentRelocationService {

	private static final Pattern FLAT_NUMBER_PATTERN = Pattern.compile("^[A-Z]-\\d{1,5}$");

	private final ResidentRelocationRequestRepository requestRepository;
	private final ResidentRelocationHistoryRepository historyRepository;
	private final ResidentRepository residentRepository;
	private final FlatRepository flatRepository;
	private final SocietyRepository societyRepository;
	private final UserRepository userRepository;
	private final com.pravesh.service.NotificationService notificationService;
	private final com.pravesh.service.SmsService smsService;
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ResidentRelocationService.class);

	private static final String UPLOAD_DIR = "uploads/relocation-proofs/";

	@Transactional
	public RelocationRequestResponse createRequest(CreateRelocationRequest req, Long residentUserId,
			MultipartFile documentFile) {

		// A resident can only have one PENDING relocation request at a time.
		requestRepository.findByResidentUserIdAndStatus(residentUserId, RequestStatus.PENDING).ifPresent(r -> {
			throw new InvalidStateException("You already have a pending relocation request");
		});

		if (req.claimedFlatNumber() == null || !FLAT_NUMBER_PATTERN.matcher(req.claimedFlatNumber()).matches()) {
			throw new InvalidStateException(
					"Flat number must look like A-101 (one capital letter, a hyphen, then up to 5 digits)");
		}

		Resident resident = residentRepository.findById(residentUserId)
				.orElseThrow(() -> new ResourceNotFoundException("Resident not found"));

		if (resident.getFlatId() == null) {
			throw new InvalidStateException("You must have a current flat before requesting relocation");
		}

		Flat oldFlat = flatRepository.findById(resident.getFlatId())
				.orElseThrow(() -> new ResourceNotFoundException("Current flat not found"));

		if (oldFlat.getSocietyId().equals(req.targetSocietyId())
				&& oldFlat.getFlatNumber().equalsIgnoreCase(req.claimedFlatNumber())) {
			throw new InvalidStateException("You are already living in this flat");
		}

		societyRepository.findById(req.targetSocietyId())
				.orElseThrow(() -> new ResourceNotFoundException("Target society not found"));

		String documentPath = saveDocument(documentFile);

		ResidentRelocationRequest request = ResidentRelocationRequest.builder().residentUserId(residentUserId)
				.oldFlatId(oldFlat.getId()).oldSocietyId(oldFlat.getSocietyId()).targetSocietyId(req.targetSocietyId())
				.claimedFlatNumber(req.claimedFlatNumber()).tower(req.tower()).documentType(req.documentType())
				.documentPath(documentPath).build();

		request = requestRepository.save(request);
		return toResponse(request);
	}

	public List<RelocationRequestResponse> getRequestsForSociety(Long societyId, RequestStatus status) {
		return requestRepository.findByTargetSocietyIdAndStatusOrderByCreatedAtDesc(societyId, status).stream()
				.map(this::toResponse).toList();
	}

	@Transactional
	public RelocationRequestResponse approve(Long requestId, Long reviewerId, boolean force) {
		ResidentRelocationRequest request = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResourceNotFoundException("Request not found"));

		if (request.getStatus() != RequestStatus.PENDING) {
			throw new InvalidStateException("This request has already been reviewed");
		}

		Resident resident = residentRepository.findById(request.getResidentUserId())
				.orElseThrow(() -> new ResourceNotFoundException("Resident not found"));

		Flat newFlat = flatRepository
				.findBySocietyIdAndFlatNumber(request.getTargetSocietyId(), request.getClaimedFlatNumber())
				.orElseGet(() -> flatRepository.save(Flat.builder().societyId(request.getTargetSocietyId())
						.flatNumber(request.getClaimedFlatNumber()).tower(request.getTower()).build()));

		boolean occupiedByOther = newFlat.getResidentId() != null
				&& !newFlat.getResidentId().equals(request.getResidentUserId());

		if (occupiedByOther && !force) {
			String occupantName = userRepository.findById(newFlat.getResidentId()).map(User::getName)
					.orElse("Unknown resident");
			throw new FlatOccupiedException(
					"Flat " + request.getClaimedFlatNumber() + " is already occupied by " + occupantName,
					newFlat.getResidentId(), occupantName, request.getClaimedFlatNumber());
		}

		String displacedNote = null;
		if (occupiedByOther) {
			Resident displaced = residentRepository.findById(newFlat.getResidentId()).orElse(null);
			if (displaced != null) {
				User displacedUser = userRepository.findById(displaced.getUserId()).orElse(null);
				String displacedName = displacedUser != null ? displacedUser.getName()
						: "resident #" + displaced.getUserId();

				displaced.setFlatId(null);
				displaced.setVerificationStatus(VerificationStatus.PENDING);
				residentRepository.save(displaced);
				displacedNote = "Displaced " + displacedName + " from flat " + request.getClaimedFlatNumber()
						+ " on admin override during this relocation.";

				if (displacedUser != null) {
					try {
						smsService.handleFlatDisplacement(new com.pravesh.dto.request.DisplacementNotifyRequest(
								displacedUser.getId(), displacedUser.getPhone(), request.getClaimedFlatNumber()));
					} catch (Exception e) {
						log.warn("Failed to notify displaced resident {}: {}", displacedUser.getId(), e.getMessage());
					}
				}
			}
		}

		// Vacate the resident's ACTUAL CURRENT flat — resolved fresh, never trusted
		// from the request's stored old_flat_id, which can go stale between
		// submission and approval if other operations happen in between.
		if (resident.getFlatId() != null) {
			flatRepository.findById(resident.getFlatId()).ifPresent(currentFlat -> {
				currentFlat.setResidentId(null);
				flatRepository.save(currentFlat);
			});
		}

		newFlat.setResidentId(request.getResidentUserId());
		flatRepository.save(newFlat);

		resident.setFlatId(newFlat.getId());
		resident.setVerificationStatus(VerificationStatus.VERIFIED);
		residentRepository.save(resident);

		request.setStatus(RequestStatus.APPROVED);
		request.setReviewedBy(reviewerId);
		request.setReviewedAt(LocalDateTime.now());
		if (displacedNote != null) {
			request.setAdminNotes(displacedNote);
		}
		requestRepository.save(request);

		historyRepository.save(ResidentRelocationHistory.builder().residentUserId(request.getResidentUserId())
				.oldFlatId(request.getOldFlatId()).oldSocietyId(request.getOldSocietyId()).newFlatId(newFlat.getId())
				.newSocietyId(request.getTargetSocietyId()).approvedBy(reviewerId).build());

		try {
			String oldFlatNumber = flatRepository.findById(request.getOldFlatId()).map(Flat::getFlatNumber).orElse("—");
			String oldSocietyName = societyRepository.findById(request.getOldSocietyId()).map(Society::getName)
					.orElse("—");
			String newSocietyName = societyRepository.findById(request.getTargetSocietyId()).map(Society::getName)
					.orElse("—");

			notificationService.handleRelocationApproved(new com.pravesh.dto.request.RelocationApprovedRequest(request.getResidentUserId(),
					newFlat.getFlatNumber(), newFlat.getTower(), newSocietyName, oldFlatNumber, oldSocietyName));
		} catch (Exception e) {
			log.warn("Failed to notify resident {} of relocation approval: {}", request.getResidentUserId(),
					e.getMessage());
		}

		return toResponse(request);
	}

	@Transactional
	public RelocationRequestResponse reject(Long requestId, Long reviewerId, String reason) {
		ResidentRelocationRequest request = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResourceNotFoundException("Request not found"));

		if (request.getStatus() != RequestStatus.PENDING) {
			throw new InvalidStateException("This request has already been reviewed");
		}

		request.setStatus(RequestStatus.REJECTED);
		request.setAdminNotes(reason);
		request.setReviewedBy(reviewerId);
		request.setReviewedAt(LocalDateTime.now());
		requestRepository.save(request);

		return toResponse(request);
	}

	public RelocationRequestResponse getMyPendingRequest(Long residentUserId) {
		return requestRepository.findByResidentUserIdAndStatus(residentUserId, RequestStatus.PENDING)
				.map(this::toResponse).orElse(null);
	}

	@Transactional
	public void revoke(Long requestId, Long residentUserId) {
		ResidentRelocationRequest request = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResourceNotFoundException("Request not found"));

		if (!request.getResidentUserId().equals(residentUserId)) {
			throw new org.springframework.security.access.AccessDeniedException("This request is not yours");
		}
		if (request.getStatus() != RequestStatus.PENDING) {
			throw new InvalidStateException("Only a pending request can be revoked");
		}

		requestRepository.delete(request);
	}

	private String saveDocument(MultipartFile file) {
		try {
			Files.createDirectories(Paths.get(UPLOAD_DIR));
			String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
			Path target = Paths.get(UPLOAD_DIR + filename);
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
			return target.toString();
		} catch (IOException e) {
			throw new RuntimeException("Failed to save document", e);
		}
	}

	private RelocationRequestResponse toResponse(ResidentRelocationRequest r) {
		String residentName = userRepository.findById(r.getResidentUserId()).map(User::getName).orElse("Unknown");
		String oldFlatNumber = flatRepository.findById(r.getOldFlatId()).map(Flat::getFlatNumber).orElse("—");
		String oldSocietyName = societyRepository.findById(r.getOldSocietyId()).map(Society::getName).orElse("—");
		String targetSocietyName = societyRepository.findById(r.getTargetSocietyId()).map(Society::getName).orElse("—");

		return new RelocationRequestResponse(r.getId(), residentName, oldFlatNumber, oldSocietyName,
				r.getClaimedFlatNumber(), targetSocietyName, r.getDocumentType(), r.getStatus(), r.getAdminNotes(),
				r.getCreatedAt());
	}

	public org.springframework.core.io.Resource getDocumentForDownload(Long requestId, Long callerId, String callerRole,
			Long callerSocietyId) {
		ResidentRelocationRequest request = requestRepository.findById(requestId)
				.orElseThrow(() -> new ResourceNotFoundException("Relocation request not found"));

		boolean isOwner = request.getResidentUserId().equals(callerId);
		boolean isAdminOfTargetSociety = "SOCIETY_ADMIN".equals(callerRole)
				&& request.getTargetSocietyId().equals(callerSocietyId);

		if (!isOwner && !isAdminOfTargetSociety) {
			throw new org.springframework.security.access.AccessDeniedException(
					"You are not permitted to access this document");
		}

		Path path = Paths.get(request.getDocumentPath());
		org.springframework.core.io.Resource resource = new org.springframework.core.io.FileSystemResource(path);
		if (!resource.exists()) {
			throw new ResourceNotFoundException("Document file not found on server");
		}
		return resource;
	}

}