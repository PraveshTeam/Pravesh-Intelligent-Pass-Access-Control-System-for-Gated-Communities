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
import com.pravesh.util.EntityRefs;
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
    private final EntityRefs refs;

    @Transactional
    public SosAlertResponse raise(CreateSosRequest req, Long residentUserId) {
        ResidentContextResponse ctx = userDirectoryService.getResidentContext(residentUserId);
        if (ctx == null || ctx.flatId() == null) {
            throw new InvalidStateException("You must have a flat assigned to raise an SOS alert");
        }

        SosAlert alert = SosAlert.builder()
                .resident(refs.ref(Resident.class, residentUserId))
                .flat(refs.ref(Flat.class, ctx.flatId()))
                .society(refs.ref(Society.class, ctx.societyId()))
                .category(SosCategory.valueOf(req.category().toUpperCase()))
                .description(req.description())
                .build();
        alert = alertRepository.save(alert);

        // First history row: the raise itself.
        recordHistory(alert, SosStatus.ACTIVE, residentUserId);

        notifySosEvent("SOS_RAISED", alert, ctx.name(), ctx.phone(), ctx.flatNumber());

        return toResponse(alert, ctx);
    }

    public List<SosAlertResponse> getActiveForSociety(Long societyId) {
        return alertRepository.findBySocietyIdAndStatusNotOrderByCreatedAtDesc(societyId, SosStatus.RESOLVED)
                .stream()
                .map(a -> toResponse(a, userDirectoryService.getResidentContext(a.getResident().getUserId())))
                .toList();
    }

    // Full incident log for a society, including RESOLVED alerts.
    public List<SosAlertResponse> getIncidentLog(Long societyId) {
        return alertRepository.findBySocietyIdOrderByCreatedAtDesc(societyId)
                .stream()
                .map(a -> toResponse(a, userDirectoryService.getResidentContext(a.getResident().getUserId())))
                .toList();
    }

    // A resident's own alerts, any status, most recent first.
    public List<SosAlertResponse> getMyAlerts(Long residentUserId) {
        ResidentContextResponse ctx = userDirectoryService.getResidentContext(residentUserId);
        return alertRepository.findByResident_UserIdOrderByCreatedAtDesc(residentUserId)
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
            alert.setAcknowledgedByUser(refs.ref(User.class, callerId));
            alert.setAcknowledgedAt(LocalDateTime.now());
        } else if (target == SosStatus.RESOLVED) {
            alert.setResolvedAt(LocalDateTime.now());
        }
        alertRepository.save(alert);

        // Record who made THIS transition, for every step.
        recordHistory(alert, target, callerId);

        ResidentContextResponse ctx = userDirectoryService.getResidentContext(alert.getResident().getUserId());
        notifySosEvent("SOS_STATUS_UPDATED", alert, ctx != null ? ctx.name() : "Unknown", null,
                ctx != null ? ctx.flatNumber() : null);

        return toResponse(alert, ctx);
    }

    // Full timeline for one alert. Restricted to the resident who raised it or
    // a GUARD/SOCIETY_ADMIN of the SAME society.
    public List<SosStatusHistoryResponse> getHistory(Long alertId, Long callerId, String callerRole, Long callerSocietyId) {
        SosAlert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found"));

        boolean isOwner = alert.getResident().getUserId().equals(callerId);
        boolean isSameSocietyResponder = ("GUARD".equals(callerRole) || "SOCIETY_ADMIN".equals(callerRole))
                && alert.getSociety().getId().equals(callerSocietyId);

        if (!isOwner && !isSameSocietyResponder) {
            throw new AccessDeniedException("You don't have access to this alert's history");
        }

        return historyRepository.findBySosAlertIdOrderByChangedAtAsc(alertId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private void recordHistory(SosAlert alert, SosStatus status, Long changedByUserId) {
        historyRepository.save(SosStatusHistory.builder()
                .sosAlert(alert)
                .status(status)
                .changedBy(refs.ref(User.class, changedByUserId))
                .build());
    }

    private SosStatusHistoryResponse toHistoryResponse(SosStatusHistory h) {
        Long actorId = h.getChangedBy().getId();
        return new SosStatusHistoryResponse(h.getStatus(), actorId, resolveActorName(actorId), h.getChangedAt());
    }

    // The actor may be a RESIDENT (initial raise) or a GUARD/SOCIETY_ADMIN.
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
        try {
            notificationService.handleSosEvent(eventType, alert.getId(), alert.getResident().getUserId(),
                    residentName, residentPhone, flatNumber, alert.getCategory().name(),
                    alert.getDescription(), alert.getStatus().name(), alert.getSociety().getId());
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
