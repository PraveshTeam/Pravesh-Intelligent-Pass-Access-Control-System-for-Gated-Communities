package com.pravesh.service;

import com.pravesh.dto.request.CreateSosRequest;
import com.pravesh.dto.response.SosAlertResponse;
import com.pravesh.dto.response.SosStatusHistoryResponse;
import com.pravesh.entity.*;
import com.pravesh.exception.InvalidStateException;
import com.pravesh.exception.ResourceNotFoundException;
import com.pravesh.dto.response.ResidentContextResponse;
import com.pravesh.dto.response.UserContactResponse;
import com.pravesh.repository.SosAlertRepository;
import com.pravesh.repository.SosStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SosService {

    private static final Logger log = LoggerFactory.getLogger(SosService.class);

    private final SosAlertRepository alertRepository;
    private final SosStatusHistoryRepository historyRepository;
    private final com.pravesh.service.NotificationService notificationService;
    private final UserDirectoryService userDirectoryService;

    @Transactional
    public SosAlertResponse raise(CreateSosRequest req, Long residentUserId) {
        ResidentContextResponse ctx = userDirectoryService.getResidentContext(residentUserId);
        if (ctx == null || ctx.flatId() == null) {
            throw new InvalidStateException("You must have a flat assigned to raise an SOS alert");
        }

        SosAlert alert = SosAlert.builder()
                .residentUserId(residentUserId)
                .flatId(ctx.flatId())
                .societyId(ctx.societyId())
                .category(SosCategory.valueOf(req.category().toUpperCase()))
                .description(req.description())
                .build();
        alert = alertRepository.save(alert);

        // First history row: the raise itself, "changed by" the resident who raised it.
        recordHistory(alert.getId(), SosStatus.ACTIVE, residentUserId);

        notifySosEvent("SOS_RAISED", alert, ctx.name(), ctx.phone(), ctx.flatNumber());

        return toResponse(alert, ctx);
    }

    public List<SosAlertResponse> getActiveForSociety(Long societyId) {
        return alertRepository.findBySocietyIdAndStatusNotOrderByCreatedAtDesc(societyId, SosStatus.RESOLVED)
                .stream()
                .map(a -> toResponse(a, userDirectoryService.getResidentContext(a.getResidentUserId())))
                .toList();
    }

    // New: the full incident log for a society -- ACTIVE, ACKNOWLEDGED,
    // HELP_ON_THE_WAY, and RESOLVED alerts alike, most recent first. This is
    // what powers the admin's SOS Incident Log screen, separate from the
    // live-banner view (which only ever shows unresolved alerts).
    public List<SosAlertResponse> getIncidentLog(Long societyId) {
        return alertRepository.findBySocietyIdOrderByCreatedAtDesc(societyId)
                .stream()
                .map(a -> toResponse(a, userDirectoryService.getResidentContext(a.getResidentUserId())))
                .toList();
    }

    // New: lets a resident see their OWN alerts (any status, most recent first) --
    // this is what powers the resident-facing live-status view. Reuses the
    // repository method that already existed but was never wired to an endpoint.
    public List<SosAlertResponse> getMyAlerts(Long residentUserId) {
        ResidentContextResponse ctx = userDirectoryService.getResidentContext(residentUserId);
        return alertRepository.findByResidentUserIdOrderByCreatedAtDesc(residentUserId)
                .stream()
                .map(a -> toResponse(a, ctx))
                .toList();
    }

    @Transactional
    public SosAlertResponse updateStatus(Long alertId, String newStatus, Long callerId) {
        SosAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        SosStatus target = SosStatus.valueOf(newStatus.toUpperCase());
        validateTransition(alert.getStatus(), target);

        alert.setStatus(target);
        if (target == SosStatus.ACKNOWLEDGED) {
            alert.setAcknowledgedBy(callerId);
            alert.setAcknowledgedAt(LocalDateTime.now());
        } else if (target == SosStatus.RESOLVED) {
            alert.setResolvedAt(LocalDateTime.now());
        }
        alertRepository.save(alert);

        // Record WHO made THIS transition, for every step -- not just the one
        // acknowledged_by column used to capture. This is the real audit trail.
        recordHistory(alert.getId(), target, callerId);

        ResidentContextResponse ctx = userDirectoryService.getResidentContext(alert.getResidentUserId());
        notifySosEvent("SOS_STATUS_UPDATED", alert, ctx != null ? ctx.name() : "Unknown", null,
                ctx != null ? ctx.flatNumber() : null);

        return toResponse(alert, ctx);
    }

    // New: full timeline for one alert. Access restricted to GUARD/SOCIETY_ADMIN
    // of the SAME society as the alert, or the resident who raised it -- nobody
    // else (learned from the earlier cross-society leaks: check the actual
    // relationship, don't just trust "any authenticated user").
    public List<SosStatusHistoryResponse> getHistory(Long alertId, Long callerId, String callerRole, Long callerSocietyId) {
        SosAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        boolean isOwner = alert.getResidentUserId().equals(callerId);
        boolean isSameSocietyResponder = ("GUARD".equals(callerRole) || "SOCIETY_ADMIN".equals(callerRole))
                && alert.getSocietyId().equals(callerSocietyId);

        if (!isOwner && !isSameSocietyResponder) {
            throw new AccessDeniedException("You don't have access to this alert's history");
        }

        return historyRepository.findBySosAlertIdOrderByChangedAtAsc(alertId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private void recordHistory(Long alertId, SosStatus status, Long changedByUserId) {
        historyRepository.save(SosStatusHistory.builder()
                .sosAlertId(alertId)
                .status(status)
                .changedByUserId(changedByUserId)
                .build());
    }

    private SosStatusHistoryResponse toHistoryResponse(SosStatusHistory h) {
        String actorName = resolveActorName(h.getChangedByUserId());
        return new SosStatusHistoryResponse(h.getStatus(), h.getChangedByUserId(), actorName, h.getChangedAt());
    }

    // The actor could be a RESIDENT (the initial raise) or a GUARD/SOCIETY_ADMIN
    // (every subsequent transition) -- try the resident-specific lookup first,
    // fall back to the generic contact lookup, and never let a name-resolution
    // failure break the whole history list.
    private String resolveActorName(Long userId) {
        try {
            ResidentContextResponse ctx = userDirectoryService.getResidentContext(userId);
            if (ctx != null && ctx.name() != null) return ctx.name();
        } catch (Exception ignored) {
            // not a resident -- fall through to the generic lookup below
        }
        try {
            UserContactResponse contact = userDirectoryService.getContact(userId);
            if (contact != null) return contact.name();
        } catch (Exception e) {
            log.warn("Could not resolve name for user {} in SOS history: {}", userId, e.getMessage());
        }
        return null;
    }

    private void notifySosEvent(String eventType, SosAlert alert, String residentName, String residentPhone,
                                   String flatNumber) {
        // Used to write an outbox row that a background poller published to
        // RabbitMQ; now it's a direct, synchronous call to NotificationService
        // (WebSocket push + guard/admin SMS) in the same request.
        try {
            notificationService.handleSosEvent(eventType, alert.getId(), alert.getResidentUserId(),
                    residentName, residentPhone, flatNumber, alert.getCategory().name(),
                    alert.getDescription(), alert.getStatus().name(), alert.getSocietyId());
        } catch (Exception e) {
            log.error("Failed to dispatch SOS event {} for alert {}: {}", eventType, alert.getId(), e.getMessage());
        }
    }

    private void validateTransition(SosStatus current, SosStatus target) {
        boolean valid = switch (current) {
            case ACTIVE -> target == SosStatus.ACKNOWLEDGED;
            case ACKNOWLEDGED -> target == SosStatus.HELP_ON_THE_WAY;
            case HELP_ON_THE_WAY -> target == SosStatus.RESOLVED;
            case RESOLVED -> false;
        };
        if (!valid) {
            throw new InvalidStateException("Cannot move from " + current + " to " + target);
        }
    }

    private SosAlertResponse toResponse(SosAlert a, ResidentContextResponse ctx) {
        return new SosAlertResponse(
                a.getId(),
                ctx != null ? ctx.name() : "Unknown",
                ctx != null ? ctx.flatNumber() : "—",
                ctx != null ? ctx.phone() : null,
                a.getCategory(), a.getDescription(), a.getStatus(),
                a.getCreatedAt(), a.getAcknowledgedAt(), a.getResolvedAt());
    }
}
