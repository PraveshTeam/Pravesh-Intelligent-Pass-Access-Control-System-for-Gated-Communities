package com.pravesh.service;

import com.pravesh.dto.response.OnboardingRequestResponse;
import com.pravesh.entity.*;
import com.pravesh.entity.enums.DocumentType;
import com.pravesh.entity.enums.RequestStatus;
import com.pravesh.entity.enums.VerificationStatus;
import com.pravesh.exception.*;
import com.pravesh.dto.request.ResidentApprovedRequest;
import com.pravesh.repository.*;
import com.pravesh.util.EntityRefs;
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
    private final DocumentStorageService documentStorageService;
    private final com.pravesh.service.NotificationService notificationService;
    private final com.pravesh.service.SmsService smsService;
    private final EntityRefs refs;

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
        if (requestRepository.existsByUser_IdAndStatus(userId, RequestStatus.PENDING)) {
            throw new DuplicateResourceException(
                    "You already have a pending onboarding request");
        }
        if (claimedFlatNumber == null || !FLAT_NUMBER_PATTERN.matcher(claimedFlatNumber).matches()) {
            throw new InvalidStateException(
                    "Flat number must look like A-101 (one capital letter, a hyphen, then up to 5 digits)");
        }

        String path = documentStorageService.store(userId, file);

        FlatAccessRequest request = FlatAccessRequest.builder()
                .user(refs.ref(User.class, userId))
                .society(refs.ref(Society.class, societyId))
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
                .findTopByUser_IdOrderByCreatedAtDesc(userId)
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

        boolean isOwner = request.getUser().getId().equals(callerId);
        boolean isAdminOfThisSociety = "SOCIETY_ADMIN".equals(callerRole)
                && request.getSociety().getId().equals(callerSocietyId);

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

    // force=false (the normal path) rejects an occupied flat with a 409;
    // force=true displaces the current occupant and reassigns the flat.
    @Transactional
    public OnboardingRequestResponse approve(Long requestId, Long reviewerId, Long callerSocietyId, boolean force) {
        FlatAccessRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Onboarding request not found"));

        if (!request.getSociety().getId().equals(callerSocietyId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not permitted to review requests for a different society");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("This request has already been reviewed");
        }

        Long requestSocietyId = request.getSociety().getId();
        Long requestUserId = request.getUser().getId();

        Flat flat = flatRepository
                .findBySocietyIdAndFlatNumber(requestSocietyId, request.getClaimedFlatNumber())
                .orElse(null);

        if (flat == null) {
            flat = Flat.builder()
                    .society(request.getSociety())
                    .flatNumber(request.getClaimedFlatNumber())
                    .tower(request.getTower())
                    .occupant(null)
                    .build();
            flat = flatRepository.save(flat);
        }

        Long currentOccupantId = flat.getOccupant() != null ? flat.getOccupant().getId() : null;
        boolean occupiedByOther = currentOccupantId != null && !currentOccupantId.equals(requestUserId);

        if (occupiedByOther && !force) {
            String occupantName = flat.getOccupant().getName();
            throw new FlatOccupiedException(
                    "Flat " + flat.getFlatNumber() + " is already occupied by " + occupantName,
                    currentOccupantId, occupantName, flat.getFlatNumber());
        }

        String displacedNote = null;
        if (occupiedByOther) {
            Resident displaced = residentRepository.findById(currentOccupantId).orElse(null);
            if (displaced != null) {
                User displacedUser = displaced.getUser();
                String displacedName = displacedUser != null ? displacedUser.getName()
                        : "resident #" + displaced.getUserId();

                displaced.setFlat(null);
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

        Resident resident = residentRepository.findById(requestUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident record not found"));

        resident.setFlat(flat);
        resident.setVerificationStatus(VerificationStatus.VERIFIED);
        resident.setMovedInDate(java.time.LocalDate.now());
        residentRepository.save(resident);

        flat.setOccupant(resident.getUser());
        flatRepository.save(flat);

        request.setStatus(RequestStatus.APPROVED);
        request.setReviewer(refs.ref(User.class, reviewerId));
        request.setReviewedAt(LocalDateTime.now());
        if (displacedNote != null) {
            request.setAdminNotes(displacedNote);
        }
        request = requestRepository.save(request);

        try {
            notificationService.handleResidentApproved(
                    new ResidentApprovedRequest(requestUserId, flat.getFlatNumber()));
        } catch (Exception e) {
            log.warn("Failed to notify resident {} of approval: {}", requestUserId, e.getMessage());
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

        if (!request.getSociety().getId().equals(callerSocietyId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not permitted to review requests for a different society");
        }

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("This request has already been reviewed");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setAdminNotes(reason);
        request.setReviewer(refs.ref(User.class, reviewerId));
        request.setReviewedAt(LocalDateTime.now());
        request = requestRepository.save(request);

        Resident resident = residentRepository.findById(request.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Resident record not found"));
        resident.setVerificationStatus(VerificationStatus.PENDING);
        residentRepository.save(resident);

        return toResponse(request);
    }

    private OnboardingRequestResponse toResponse(FlatAccessRequest request) {
        User user = request.getUser();
        String userName = user != null ? user.getName() : "Unknown";

        return new OnboardingRequestResponse(
                request.getId(),
                user != null ? user.getId() : null,
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