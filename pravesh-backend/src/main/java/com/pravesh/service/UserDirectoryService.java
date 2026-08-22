package com.pravesh.service;

import com.pravesh.dto.response.EmergencyContactResponse;
import com.pravesh.dto.response.FlatInternalResponse;
import com.pravesh.dto.response.ResidentContextResponse;
import com.pravesh.dto.response.ShiftStatusResponse;
import com.pravesh.dto.response.UserContactResponse;
import com.pravesh.entity.Flat;
import com.pravesh.entity.Gate;
import com.pravesh.entity.GuardShift;
import com.pravesh.entity.Resident;
import com.pravesh.entity.User;
import com.pravesh.exception.ResourceNotFoundException;
import com.pravesh.repository.FlatRepository;
import com.pravesh.repository.GateRepository;
import com.pravesh.repository.GuardShiftRepository;
import com.pravesh.repository.ResidentRepository;
import com.pravesh.repository.SocietyAdminRepository;
import com.pravesh.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Consolidates what used to be three separate HTTP-only controllers
 * (InternalController, InternalResidentController, InternalSocietyController in
 * user-service) that existed solely so other microservices could reach user-service
 * data via Feign + the API gateway.
 *
 * Now that everything runs in one JVM, those other services (SosService, TripService,
 * ForumService, PaymentService, PassService, ValidationService, NotificationService...)
 * just autowire this service directly instead of making an HTTP call.
 */
@Service
@RequiredArgsConstructor
public class UserDirectoryService {

    private final FlatRepository flatRepository;
    private final ShiftCheckinService shiftCheckinService;
    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final GateRepository gateRepository;
    private final GuardShiftRepository guardShiftRepository;
    private final SocietyAdminRepository societyAdminRepository;

    public FlatInternalResponse getFlat(Long id) {
        Flat flat = flatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flat not found"));
        return new FlatInternalResponse(flat.getId(), flat.getSocietyId(),
                flat.getFlatNumber(), flat.getTower(), flat.getResidentId());
    }

    public ShiftStatusResponse getShiftStatus(Long guardUserId) {
        var activeShiftId = shiftCheckinService.getActiveShiftId(guardUserId);
        return new ShiftStatusResponse(activeShiftId.isPresent(), activeShiftId.orElse(null));
    }

    public UserContactResponse getContact(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserContactResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone());
    }

    public String getFlatNumber(Long residentId) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));

        if (resident.getFlatId() == null) {
            return "Unassigned";
        }
        return flatRepository.findById(resident.getFlatId()).map(Flat::getFlatNumber).orElse("Unknown");
    }

    public ResidentContextResponse getResidentContext(Long userId) {
        Resident resident = residentRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String flatNumber = null;
        Long societyId = null;
        if (resident.getFlatId() != null) {
            Flat flat = flatRepository.findById(resident.getFlatId()).orElse(null);
            if (flat != null) {
                flatNumber = flat.getFlatNumber();
                societyId = flat.getSocietyId();
            }
        }

        return new ResidentContextResponse(
                user.getId(), user.getName(), user.getPhone(),
                resident.getFlatId(), flatNumber, societyId);
    }

    public EmergencyContactResponse getEmergencyContact(Long societyId) {
        List<Gate> gates = gateRepository.findBySocietyId(societyId);

        for (Gate gate : gates) {
            Optional<GuardShift> activeShift = guardShiftRepository
                    .findTopByGateIdAndShiftEndIsNullOrderByShiftStartDesc(gate.getId());
            if (activeShift.isPresent()) {
                Long guardUserId = activeShift.get().getGuardUserId();
                User guardUser = userRepository.findById(guardUserId).orElse(null);
                if (guardUser != null) {
                    return new EmergencyContactResponse("GUARD", guardUser.getName(), guardUser.getPhone());
                }
            }
        }

        // No guard currently on duty anywhere in this society — fall back to admin.
        return societyAdminRepository.findBySocietyId(societyId).stream()
                .findFirst()
                .map(admin -> {
                    User adminUser = userRepository.findById(admin.getUserId()).orElse(null);
                    return adminUser != null
                            ? new EmergencyContactResponse("ADMIN", adminUser.getName(), adminUser.getPhone())
                            : new EmergencyContactResponse("NONE", null, null);
                })
                .orElse(new EmergencyContactResponse("NONE", null, null));
    }
}
