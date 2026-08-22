package com.pravesh.service;

import com.pravesh.dto.request.CreateGuardRequest;
import com.pravesh.dto.request.ReassignGateRequest;
import com.pravesh.dto.response.GuardResponse;
import com.pravesh.entity.Gate;
import com.pravesh.entity.Guard;
import com.pravesh.entity.Society;
import com.pravesh.entity.User;
import com.pravesh.entity.enums.Role;
import com.pravesh.exception.DuplicateResourceException;
import com.pravesh.exception.InvalidStateException;
import com.pravesh.exception.ResourceNotFoundException;
import com.pravesh.dto.request.GuardCredentialsRequest;
import com.pravesh.repository.GateRepository;
import com.pravesh.repository.GuardRepository;
import com.pravesh.repository.UserRepository;
import com.pravesh.util.EntityRefs;
import com.pravesh.util.TempPasswordGenerator;
import lombok.RequiredArgsConstructor;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuardManagementService {

    private final UserRepository userRepository;
    private final GuardRepository guardRepository;
    private final GateRepository gateRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.pravesh.service.NotificationService notificationService;
    private final EntityRefs refs;
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GuardManagementService.class);

    @Transactional
    public GuardResponse createGuard(CreateGuardRequest req, Long createdByAdminId, Long callerSocietyId) {
        if (userRepository.existsByPhone(req.phone())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        Gate gate;

        if (req.gateId() != null) {
            gate = gateRepository.findById(req.gateId())
                    .orElseThrow(() -> new ResourceNotFoundException("Gate not found"));

            if (!gate.getSociety().getId().equals(callerSocietyId)) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "You cannot assign a guard to a gate outside your own society");
            }
        } else {
            if (req.newGateName() == null || req.newGateName().isBlank()) {
                throw new InvalidStateException(
                        "Either gateId or newGateName must be provided");
            }
            gate = Gate.builder()
                    .society(refs.ref(Society.class, callerSocietyId))
                    .name(req.newGateName())
                    .location(req.newGateLocation())
                    .build();
            gate = gateRepository.save(gate);
        }

        if (guardRepository.existsByGateId(gate.getId())) {
            throw new DuplicateResourceException("This gate already has an assigned guard");
        }

        String tempPassword = TempPasswordGenerator.generate();
        String tempEmail = "guard." + req.phone() + "@pravesh.local";

        User user = User.builder()
                .name(req.name())
                .email(tempEmail)
                .phone(req.phone())
                .passwordHash(passwordEncoder.encode(tempPassword))
                .role(Role.GUARD)
                .state("N/A")
                .isActive(true)
                .build();
        user = userRepository.save(user);

        Guard guard = Guard.builder()
                .user(user)
                .gate(gate)
                .createdByAdmin(refs.ref(User.class, createdByAdminId))
                .build();
        guardRepository.save(guard);

        try {
            notificationService.handleGuardCredentials(
                    new GuardCredentialsRequest(req.phone(), tempPassword, gate.getName()));
        } catch (Exception e) {
            log.warn("Failed to send guard credentials SMS to {}: {}", req.phone(), e.getMessage());
            // Fallback so you can still test locally if SMS delivery fails
            System.out.println("[DEV FALLBACK] Guard temp credentials — phone: " + req.phone()
                    + " tempPassword: " + tempPassword);
        }

        return new GuardResponse(user.getId(), user.getName(), user.getPhone(),
                gate.getId(), gate.getName(), guard.getEmployeeCode(), user.isActive());
    }

    public List<GuardResponse> listGuards(Long societyId) {
        return guardRepository.findAll().stream()
                .map(g -> {
                    Gate gate = g.getGate();
                    if (gate == null) {
                        throw new ResourceNotFoundException("Gate not found");
                    }
                    if (!gate.getSociety().getId().equals(societyId)) {
                        return null;
                    }
                    User u = g.getUser();
                    if (u == null) {
                        throw new ResourceNotFoundException("User not found");
                    }
                    return new GuardResponse(u.getId(), u.getName(), u.getPhone(),
                            gate.getId(), gate.getName(), g.getEmployeeCode(), u.isActive());
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Transactional
    public GuardResponse reassignGate(Long guardUserId, ReassignGateRequest req, Long callerSocietyId) {
        Guard guard = guardRepository.findById(guardUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Guard not found"));

        if (guardRepository.existsByGateId(req.newGateId())) {
            throw new DuplicateResourceException("This gate already has an assigned guard");
        }

        Gate newGate = gateRepository.findById(req.newGateId())
                .orElseThrow(() -> new ResourceNotFoundException("Gate not found"));

        if (!newGate.getSociety().getId().equals(callerSocietyId)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You cannot reassign a guard to a gate outside your own society");
        }

        guard.setGate(newGate);
        guardRepository.save(guard);

        User u = guard.getUser();
        if (u == null) {
            throw new ResourceNotFoundException("User not found");
        }

        return new GuardResponse(u.getId(), u.getName(), u.getPhone(),
                newGate.getId(), newGate.getName(), guard.getEmployeeCode(), u.isActive());
    }
}