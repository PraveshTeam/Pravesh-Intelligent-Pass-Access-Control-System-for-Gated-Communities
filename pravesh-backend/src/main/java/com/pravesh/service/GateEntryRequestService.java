package com.pravesh.service;

import com.pravesh.dto.request.CreateGateEntryRequest;
import com.pravesh.dto.response.GateEntryRequestResponse;
import com.pravesh.dto.response.ResidentDirectoryEntry;
import com.pravesh.entity.*;
import com.pravesh.exception.*;
import com.pravesh.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GateEntryRequestService {

    private static final Logger log = LoggerFactory.getLogger(GateEntryRequestService.class);

    private final GateEntryRequestRepository gateEntryRequestRepository;
    private final FlatRepository flatRepository;
    private final GuardRepository guardRepository;
    private final com.pravesh.service.NotificationService notificationService;
    private final com.pravesh.service.ValidationService validationService;

    @Transactional
    public GateEntryRequestResponse createRequest(CreateGateEntryRequest req, Long guardUserId, Long societyId) {
        Guard guard = guardRepository.findById(guardUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Guard not found"));

        Flat flat = flatRepository.findBySocietyIdAndFlatNumber(societyId, req.claimedFlatNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No flat " + req.claimedFlatNumber() + " found in this society"));

        if (flat.getResidentId() == null) {
            throw new InvalidStateException("Flat " + req.claimedFlatNumber() + " has no resident on record");
        }

        GateEntryRequest entry = GateEntryRequest.builder()
                .societyId(societyId)
                .gateId(guard.getGateId())
                .guardUserId(guardUserId)
                .visitorName(req.visitorName())
                .visitorPhone(req.visitorPhone())
                .claimedFlatNumber(req.claimedFlatNumber())
                .reason(req.reason())
                .flatId(flat.getId())
                .residentId(flat.getResidentId())
                .build();

        entry = gateEntryRequestRepository.save(entry);

        try {
            // Was: userRepository.findById(flat.getResidentId()) -- now a
            // single navigation off the already-loaded Flat (occupant).
            var resident = flat.getOccupant();
            if (resident != null) {
                notificationService.handleGateEntryRequest(new com.pravesh.dto.request.GateEntryNotifyRequest(
                        resident.getId(), resident.getPhone(),
                        req.visitorName(), req.claimedFlatNumber(), entry.getId()));
            }
        } catch (Exception e) {
            log.warn("Failed to notify resident of gate entry request {}: {}", entry.getId(), e.getMessage());
        }

        return toResponse(entry);
    }

    public GateEntryRequestResponse getStatus(Long id, Long guardUserId) {
        GateEntryRequest entry = gateEntryRequestRepository.findByIdAndGuardUserId(id, guardUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));
        return toResponse(entry);
    }

    public List<GateEntryRequestResponse> getMyPendingRequests(Long residentId) {
        return gateEntryRequestRepository
                .findByResidentIdAndStatusOrderByCreatedAtDesc(residentId, GateRequestStatus.PENDING)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public GateEntryRequestResponse respond(Long id, Long residentId, boolean approve) {
        GateEntryRequest entry = gateEntryRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!entry.getResidentId().equals(residentId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "This request is not addressed to you");
        }
        if (entry.getStatus() != GateRequestStatus.PENDING) {
            throw new InvalidStateException("This request has already been " + entry.getStatus());
        }
        if (entry.getExpiresAt().isBefore(LocalDateTime.now())) {
            entry.setStatus(GateRequestStatus.EXPIRED);
            gateEntryRequestRepository.save(entry);
            throw new InvalidStateException("This request has expired");
        }

        entry.setStatus(approve ? GateRequestStatus.APPROVED : GateRequestStatus.DENIED);
        entry.setRespondedAt(LocalDateTime.now());
        gateEntryRequestRepository.save(entry);

        try {
            validationService.logWalkInEntry(new com.pravesh.dto.request.WalkInEntryLogRequest(
                    entry.getResidentId(), entry.getVisitorName(), entry.getGuardUserId(), entry.getGateId(),
                    entry.getSocietyId(), approve ? "GRANTED" : "DENIED",
                    approve ? null : "RESIDENT_DENIED"));
        } catch (Exception e) {
            log.warn("Failed to log walk-in entry for gate entry request {}: {}", entry.getId(), e.getMessage());
        }

        return toResponse(entry);
    }

    // Runs every 30 seconds — auto-expires anything the resident never answered.
    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void expireStaleRequests() {
        var stale = gateEntryRequestRepository
                .findByStatusAndExpiresAtBefore(GateRequestStatus.PENDING, LocalDateTime.now());
        for (GateEntryRequest entry : stale) {
            entry.setStatus(GateRequestStatus.EXPIRED);
        }
        if (!stale.isEmpty()) {
            gateEntryRequestRepository.saveAll(stale);
            for (GateEntryRequest entry : stale) {
                try {
                    validationService.logWalkInEntry(new com.pravesh.dto.request.WalkInEntryLogRequest(
                            entry.getResidentId(), entry.getVisitorName(), entry.getGuardUserId(),
                            entry.getGateId(), entry.getSocietyId(), "NO_RESPONSE", "RESIDENT_NO_RESPONSE"));
                } catch (Exception e) {
                    log.warn("Failed to log expired walk-in entry for gate entry request {}: {}",
                            entry.getId(), e.getMessage());
                }
            }
        }
    }

    private GateEntryRequestResponse toResponse(GateEntryRequest e) {
        return new GateEntryRequestResponse(
                e.getId(), e.getVisitorName(), e.getVisitorPhone(), e.getClaimedFlatNumber(),
                e.getReason(), e.getStatus(), e.getCreatedAt(), e.getExpiresAt());
    }
    
    public List<ResidentDirectoryEntry> getSocietyResidents(Long societyId) {
        return flatRepository.findBySocietyId(societyId).stream()
                .filter(f -> f.getResidentId() != null)
                .map(f -> {
                    // Was: userRepository.findById(f.getResidentId()) -- now a
                    // single navigation off the already-loaded Flat (occupant).
                    var user = f.getOccupant();
                    if (user == null) return null;
                    return new ResidentDirectoryEntry(
                            user.getId(), user.getName(), user.getPhone(),
                            f.getId(), f.getFlatNumber(), f.getTower());
                })
                .filter(java.util.Objects::nonNull)
                .sorted(java.util.Comparator.comparing(ResidentDirectoryEntry::flatNumber))
                .toList();
    }
}