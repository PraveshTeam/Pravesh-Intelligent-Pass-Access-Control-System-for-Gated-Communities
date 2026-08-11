package com.pravesh.service;

import com.pravesh.dto.response.OnboardingRequestResponse;
import com.pravesh.entity.*;
import com.pravesh.entity.enums.DocumentType;
import com.pravesh.entity.enums.RequestStatus;
import com.pravesh.entity.enums.VerificationStatus;
import com.pravesh.exception.*;
import com.pravesh.dto.request.ResidentApprovedRequest;
import com.pravesh.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private static final Logger log = LoggerFactory.getLogger(OnboardingService.class);
    private static final Pattern FLAT_NUMBER_PATTERN = Pattern.compile("^[A-Z]-\\d{1,5}$");

    private final FlatAccessRequestRepository requestRepository;
    private final ResidentRepository residentRepository;
    private final FlatRepository flatRepository;
    private final UserRepository userRepository;
    private final DocumentStorageService documentStorageService;
    private final com.pravesh.service.NotificationService notificationService;
    private final com.pravesh.service.SmsService smsService;

    @Transactional
    public OnboardingRequestResponse submitRequest(
            Long userId, Long societyId, String claimedFlatNumber, String tower,
            DocumentType documentType, MultipartFile file) {

        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident record not found"));

        if (resident.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new InvalidStateException(
                    "Onboarding request not allowed — account is already " +
                            resident.getVerificationStatus());
        }
        if (requestRepository.existsByUserIdAndStatus(userId, RequestStatus.PENDING)) {
            throw new DuplicateResourceException(
                    "You already have a pending onboarding request");
        }
        if (claimedFlatNumber == null || !FLAT_NUMBER_PATTERN.matcher(claimedFlatNumber).matches()) {
            throw new InvalidStateException(
                    "Flat number must look like A-101 (one capital letter, a hyphen, then up to 5 digits)");
        }

        String path = documentStorageService.store(userId, file);

        FlatAccessRequest request = FlatAccessRequest.builder()
                .userId(userId)
                .societyId(societyId)
                .claimedFlatNumber(claimedFlatNumber)
                .tower(tower)
                .documentType(documentType)
                .documentPath(path)
                .status(RequestStatus.PENDING)
                .build();

        request = requestRepository.save(request);
        return toResponse(request);
    }

    public OnboardingRequestResponse getMyLatestRequest(Long userId) {
        FlatAccessRequest request = requestRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No onboarding request found"));
        return toResponse(request);
    }

    public List<OnboardingRequestResponse> listByStatus(RequestStatus status, Long societyId) {
        return requestRepository.findByStatusAndSocietyId(status, societyId).stream()
                .map(this::toResponse)
                .toList();
    }

    public Resource getDocumentForDownload(Long requestId, Long callerId, String callerRole, Long callerSocietyId) {
        FlatAccessRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding request not found"));

        boolean isOwner = request.getUserId().equals(callerId);
        boolean isAdminOfThisSociety = "SOCIETY_ADMIN".equals(callerRole)
                && request.getSocietyId().equals(callerSocietyId);

        if (!isOwner && !isAdminOfThisSociety) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not permitted to access this document");
        }

        Path path = Path.of(request.getDocumentPath());
        Resource resource = new FileSystemResource(path);
        if (!resource.exists()) {
            throw new ResourceNotFoundException("Document file not found on server");
        }
        return resource;
    }

    /**
     * `force` mirrors ResidentRelocationService.approve()'s exact pattern:
     * without it, an occupied flat throws FlatOccupiedException (409, with
     * enough detail for the admin to see who's currently there) instead of
     * the old blunt DuplicateResourceException dead-end. With force=true,
     * the current occupant is displaced (flatId cleared, verificationStatus
     * reset to PENDING -- same consequence, same notification path, as a
     * relocation-caused displacement) and the new resident takes the flat.
     *
     * This does NOT touch ResidentRelocationService or its swap/race-condition
     * logic at all -- it brings onboarding's conflict handling up to the same
     * standard that flow already had, nothing more.
     */
    @Transactional
    public OnboardingRequestResponse approve(Long requestId, Long reviewerId, Long callerSocietyId, boolean force) {
        FlatAccessRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding request not found"));

        if (!request.getSocietyId().equals(callerSocietyId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not permitted to review requests for a different society");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("This request has already been reviewed");
        }

        Flat flat = flatRepository
                .findBySocietyIdAndFlatNumber(request.getSocietyId(), request.getClaimedFlatNumber())
                .orElse(null);

        if (flat == null) {
            flat = Flat.builder()
                    .societyId(request.getSocietyId())
                    .flatNumber(request.getClaimedFlatNumber())
                    .tower(request.getTower())
                    .residentId(null)
                    .build();
            flat = flatRepository.save(flat);
        }

        boolean occupiedByOther = flat.getResidentId() != null
                && !flat.getResidentId().equals(request.getUserId());

        if (occupiedByOther && !force) {
            String occupantName = userRepository.findById(flat.getResidentId())
                    .map(User::getName)
                    .orElse("Unknown resident");
            throw new FlatOccupiedException(
                    "Flat " + flat.getFlatNumber() + " is already occupied by " + occupantName,
                    flat.getResidentId(), occupantName, flat.getFlatNumber());
        }

        String displacedNote = null;
        if (occupiedByOther) {
            Resident displaced = residentRepository.findById(flat.getResidentId()).orElse(null);
            if (displaced != null) {
                User displacedUser = userRepository.findById(displaced.getUserId()).orElse(null);
                String displacedName = displacedUser != null ? displacedUser.getName()
                        : "resident #" + displaced.getUserId();

                displaced.setFlatId(null);
                displaced.setVerificationStatus(VerificationStatus.PENDING);
                residentRepository.save(displaced);
                displacedNote = "Displaced " + displacedName + " from flat " + flat.getFlatNumber()
                        + " on admin override during onboarding approval.";

                if (displacedUser != null) {
                    try {
                        smsService.handleFlatDisplacement(new com.pravesh.dto.request.DisplacementNotifyRequest(
                                displacedUser.getId(), displacedUser.getPhone(), flat.getFlatNumber()));
                    } catch (Exception e) {
                        log.warn("Failed to notify displaced resident {}: {}", displacedUser.getId(), e.getMessage());
                    }
                }
            }
        }

        Resident resident = residentRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Resident record not found"));

        resident.setFlatId(flat.getId());
        resident.setVerificationStatus(VerificationStatus.VERIFIED);
        resident.setMovedInDate(java.time.LocalDate.now());
        residentRepository.save(resident);

        flat.setResidentId(request.getUserId());
        flatRepository.save(flat);

        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedBy(reviewerId);
        request.setReviewedAt(LocalDateTime.now());
        if (displacedNote != null) {
            request.setAdminNotes(displacedNote);
        }
        request = requestRepository.save(request);

        try {
            notificationService.handleResidentApproved(
                    new ResidentApprovedRequest(request.getUserId(), flat.getFlatNumber()));
        } catch (Exception e) {
            log.warn("Failed to notify resident {} of approval: {}",
                    request.getUserId(), e.getMessage());
        }

        return toResponse(request);
    }

    @Transactional
    public OnboardingRequestResponse reject(Long requestId, Long reviewerId, String reason, Long callerSocietyId) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidStateException("A rejection reason is required");
        }

        FlatAccessRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding request not found"));

        if (!request.getSocietyId().equals(callerSocietyId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not permitted to review requests for a different society");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("This request has already been reviewed");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setAdminNotes(reason);
        request.setReviewedBy(reviewerId);
        request.setReviewedAt(LocalDateTime.now());
        request = requestRepository.save(request);

        Resident resident = residentRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Resident record not found"));
        resident.setVerificationStatus(VerificationStatus.PENDING);
        residentRepository.save(resident);

        return toResponse(request);
    }

    private OnboardingRequestResponse toResponse(FlatAccessRequest request) {
        String userName = userRepository.findById(request.getUserId())
                .map(User::getName)
                .orElse("Unknown");

        return new OnboardingRequestResponse(
                request.getId(),
                request.getUserId(),
                userName,
                request.getClaimedFlatNumber(),
                request.getTower(),
                request.getDocumentType().name(),
                request.getStatus().name(),
                request.getAdminNotes(),
                request.getCreatedAt(),
                request.getReviewedAt()
        );
    }
}