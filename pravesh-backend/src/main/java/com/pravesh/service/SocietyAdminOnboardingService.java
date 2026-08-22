package com.pravesh.service;

import com.pravesh.dto.response.SocietyRegistrationResponse;
import com.pravesh.entity.Society;
import com.pravesh.entity.SocietyAdmin;
import com.pravesh.entity.SocietyRegistrationRequest;
import com.pravesh.entity.enums.RequestStatus;
import com.pravesh.entity.enums.VerificationStatus;
import com.pravesh.exception.*;
import com.pravesh.dto.request.SocietyAdminApprovedRequest;
import com.pravesh.repository.SocietyAdminRepository;
import com.pravesh.repository.SocietyRegistrationRequestRepository;
import com.pravesh.repository.SocietyRepository;
import com.pravesh.entity.User;
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

@Service
@RequiredArgsConstructor
public class SocietyAdminOnboardingService {

    private static final Logger log = LoggerFactory.getLogger(SocietyAdminOnboardingService.class);

    private final SocietyRegistrationRequestRepository requestRepository;
    private final SocietyAdminRepository societyAdminRepository;
    private final SocietyRepository societyRepository;
    private final DocumentStorageService documentStorageService;
    private final com.pravesh.service.NotificationService notificationService;
    private final EntityRefs refs;

    @Transactional
    public SocietyRegistrationResponse submitRequest(
            Long adminUserId, String societyName, String address, String city, MultipartFile file) {

        SocietyAdmin admin = societyAdminRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Society admin record not found"));

        if (admin.getVerificationStatus() != VerificationStatus.PENDING) {
            throw new InvalidStateException(
                    "Request not allowed — account is already " + admin.getVerificationStatus());
        }
        if (requestRepository.existsByAdminUser_IdAndStatus(adminUserId, RequestStatus.PENDING)) {
            throw new DuplicateResourceException("You already have a pending society registration request");
        }

        String path = documentStorageService.store(adminUserId, file);

        SocietyRegistrationRequest request = SocietyRegistrationRequest.builder()
                .adminUser(refs.ref(User.class, adminUserId))
                .societyName(societyName)
                .address(address)
                .city(city)
                .documentPath(path)
                .status(RequestStatus.PENDING)
                .build();

        request = requestRepository.save(request);
        return toResponse(request);
    }

    public SocietyRegistrationResponse getMyLatestRequest(Long adminUserId) {
        SocietyRegistrationRequest request = requestRepository
                .findTopByAdminUser_IdOrderByCreatedAtDesc(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No society registration request found"));
        return toResponse(request);
    }

    public List<SocietyRegistrationResponse> listByStatus(RequestStatus status) {
        return requestRepository.findByStatus(status).stream()
                .map(this::toResponse)
                .toList();
    }

    public Resource getDocumentForDownload(Long requestId, Long callerId, String callerRole) {
        SocietyRegistrationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        boolean isOwner = request.getAdminUser().getId().equals(callerId);
        boolean isSuperAdmin = "SUPER_ADMIN".equals(callerRole);

        if (!isOwner && !isSuperAdmin) {
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

    @Transactional
    public SocietyRegistrationResponse approve(Long requestId, Long reviewerId) {
        SocietyRegistrationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("This request has already been reviewed");
        }

        if (societyRepository.existsByNameAndCity(request.getSocietyName(), request.getCity())) {
            throw new DuplicateResourceException(
                    "A society named '" + request.getSocietyName() +
                    "' already exists in " + request.getCity());
        }

        Society society = Society.builder()
                .name(request.getSocietyName())
                .address(request.getAddress())
                .city(request.getCity())
                .build();
        society = societyRepository.save(society);


        SocietyAdmin admin = societyAdminRepository.findById(request.getAdminUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Society admin record not found"));

        admin.setSociety(society);
        admin.setVerificationStatus(VerificationStatus.VERIFIED);
        societyAdminRepository.save(admin);

        request.setStatus(RequestStatus.APPROVED);
        request.setReviewer(refs.ref(User.class, reviewerId));
        request.setReviewedAt(LocalDateTime.now());
        request = requestRepository.save(request);

        try {
            notificationService.handleSocietyAdminApproved(
                    new SocietyAdminApprovedRequest(request.getAdminUser().getId(), society.getName()));
        } catch (Exception e) {
            log.warn("Failed to notify society admin {} of approval: {}",
                    request.getAdminUser().getId(), e.getMessage());
        }

        return toResponse(request);
    }

    @Transactional
    public SocietyRegistrationResponse reject(Long requestId, Long reviewerId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new InvalidStateException("A rejection reason is required");
        }

        SocietyRegistrationRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new InvalidStateException("This request has already been reviewed");
        }

        request.setStatus(RequestStatus.REJECTED);
        request.setAdminNotes(reason);
        request.setReviewer(refs.ref(User.class, reviewerId));
        request.setReviewedAt(LocalDateTime.now());
        request = requestRepository.save(request);

        SocietyAdmin admin = societyAdminRepository.findById(request.getAdminUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Society admin record not found"));
        admin.setVerificationStatus(VerificationStatus.PENDING);
        societyAdminRepository.save(admin);

        return toResponse(request);
    }

    private SocietyRegistrationResponse toResponse(SocietyRegistrationRequest request) {
        User adminUser = request.getAdminUser();
        String adminName = adminUser != null ? adminUser.getName() : "Unknown";

        return new SocietyRegistrationResponse(
                request.getId(), adminUser != null ? adminUser.getId() : null, adminName,
                request.getSocietyName(), request.getAddress(), request.getCity(),
                request.getStatus().name(), request.getAdminNotes(),
                request.getCreatedAt(), request.getReviewedAt());
    }
}