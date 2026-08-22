package com.pravesh.service;

import com.pravesh.entity.EntryLog;
import com.pravesh.entity.Gate;
import com.pravesh.entity.Guard;
import com.pravesh.entity.GuardShift;
import com.pravesh.entity.Resident;
import com.pravesh.entity.Society;
import com.pravesh.entity.VisitorPass;
import com.pravesh.entity.enums.EntryType;
import com.pravesh.entity.enums.ScanResult;
import com.pravesh.exception.ShiftRequiredException;
import com.pravesh.dto.request.VisitorEnteredRequest;
import com.pravesh.dto.response.PassValidationResponse;
import com.pravesh.dto.response.ShiftStatusResponse;
import com.pravesh.repository.EntryLogRepository;
import com.pravesh.util.EntityRefs;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ValidationService {

    private static final Logger log = LoggerFactory.getLogger(ValidationService.class);

    private final com.pravesh.service.PassService passService;
    private final com.pravesh.service.UserDirectoryService userDirectoryService;
    private final com.pravesh.service.NotificationService notificationService;
    private final EntryLogRepository entryLogRepository;
    private final EntityRefs refs;

    public PassValidationResponse scan(String uuid, Long guardId, Long gateId, Long societyId) {
        ShiftStatusResponse shiftStatus = userDirectoryService.getShiftStatus(guardId);

        if (!shiftStatus.hasActiveShift()) {
            throw new ShiftRequiredException(
                    "You must check in for your shift before scanning");
        }

        PassValidationResponse result = passService.validateAndConsume(uuid, societyId);

        EntryLog entryLog = EntryLog.builder()
                .visitorPass(refs.ref(VisitorPass.class, result.passId()))
                .resident(refs.ref(Resident.class, result.residentId()))
                .visitorName(result.visitorName())
                .guard(refs.ref(Guard.class, guardId))
                .gate(refs.ref(Gate.class, gateId))
                .society(refs.ref(Society.class, societyId))
                .shift(refs.ref(GuardShift.class, shiftStatus.shiftId()))
                .scanResult(result.granted() ? ScanResult.GRANTED : ScanResult.DENIED)
                .denyReason(result.granted() ? null : result.reason())
                .build();

        entryLogRepository.save(entryLog);

        if (result.granted()) {
            try {
                notificationService.handleVisitorEntered(new VisitorEnteredRequest(
                        result.residentId(), result.visitorName(),
                        "Gate " + gateId, LocalDateTime.now().toString()));
            } catch (Exception e) {
                log.warn("Failed to notify Notification-Service of visitor entry for pass {}: {}",
                        result.passId(), e.getMessage());
            }
        }

        return result;
    }

    public List<EntryLog> getEntriesByGate(Long gateId, java.time.LocalDate date, Long societyId) {
        java.time.LocalDateTime start = date.atStartOfDay();
        java.time.LocalDateTime end = date.plusDays(1).atStartOfDay();
        return entryLogRepository.findByGateIdAndScannedAtBetweenAndSocietyId(gateId, start, end, societyId);
    }

    public List<EntryLog> getEntriesByFlat(Long residentId, Long societyId) {
        return entryLogRepository.findByResident_UserIdAndSocietyId(residentId, societyId);
    }

    public List<EntryLog> getAllEntriesInSociety(Long societyId) {
        return entryLogRepository.findBySocietyId(societyId);
    }

    public void logWalkInEntry(com.pravesh.dto.request.WalkInEntryLogRequest req) {
        EntryLog entryLog = EntryLog.builder()
                .entryType(EntryType.WALK_IN)
                .resident(refs.ref(Resident.class, req.residentId()))
                .visitorName(req.visitorName())
                .guard(refs.ref(Guard.class, req.guardId()))
                .gate(refs.ref(Gate.class, req.gateId()))
                .society(refs.ref(Society.class, req.societyId()))
                .scanResult(ScanResult.valueOf(req.outcome()))
                .denyReason(req.denyReason())
                .build();
        entryLogRepository.save(entryLog);
    }
}