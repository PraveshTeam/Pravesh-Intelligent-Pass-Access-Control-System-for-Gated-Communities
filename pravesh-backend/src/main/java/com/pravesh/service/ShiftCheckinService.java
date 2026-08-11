package com.pravesh.service;

import com.pravesh.dto.request.ShiftCheckinRequest;
import com.pravesh.dto.response.GuardShiftResponse;
import com.pravesh.dto.response.ShiftCheckinResponse;
import com.pravesh.entity.Guard;
import com.pravesh.entity.GuardShift;
import com.pravesh.exception.ResourceNotFoundException;
import com.pravesh.repository.GuardRepository;
import com.pravesh.repository.GuardShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShiftCheckinService {

    private final GuardShiftRepository shiftRepository;
    private final GuardRepository guardRepository;

    @Transactional
    public ShiftCheckinResponse checkIn(Long guardUserId, ShiftCheckinRequest req) {
        Guard guard = guardRepository.findById(guardUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Guard record not found"));

        // Close out any stale open shift (e.g. app crashed without a clean logout)
        shiftRepository.findTopByGuardUserIdAndShiftEndIsNullOrderByShiftStartDesc(guardUserId)
                .ifPresent(open -> {
                    open.setShiftEnd(LocalDateTime.now());
                    shiftRepository.save(open);
                });

        GuardShift shift = GuardShift.builder()
                .guardUserId(guardUserId)
                .gateId(guard.getGateId())
                .onDutyName(req.onDutyName())
                .onDutyEmployeeId(req.onDutyEmployeeId())
                .build();
        shift = shiftRepository.save(shift);

        return new ShiftCheckinResponse(shift.getId(), shift.getGateId(),
                shift.getOnDutyName(), shift.getShiftStart());
    }

    /** Used by the shift-required check (428 gate) — returns the active shiftId or empty. */
    public Optional<Long> getActiveShiftId(Long guardUserId) {
        return shiftRepository
                .findTopByGuardUserIdAndShiftEndIsNullOrderByShiftStartDesc(guardUserId)
                .map(GuardShift::getId);
    }

    public List<GuardShiftResponse> getShiftHistory(Long guardUserId) {
        return shiftRepository.findByGuardUserIdOrderByShiftStartDesc(guardUserId).stream()
                .map(s -> new GuardShiftResponse(s.getId(), s.getOnDutyName(),
                        s.getOnDutyEmployeeId(), s.getShiftStart(), s.getShiftEnd()))
                .toList();
    }
    
    @Transactional
    public void endShift(Long guardUserId) {
        shiftRepository.findTopByGuardUserIdAndShiftEndIsNullOrderByShiftStartDesc(guardUserId)
                .ifPresentOrElse(shift -> {
                    shift.setShiftEnd(LocalDateTime.now());
                    shiftRepository.save(shift);
                }, () -> {
                    throw new ResourceNotFoundException("No active shift to end");
                });
    }
    
}