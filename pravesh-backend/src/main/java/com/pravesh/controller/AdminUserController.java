package com.pravesh.controller;

import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.UserProfileResponse;
import com.pravesh.entity.User;
import com.pravesh.entity.enums.Role;
import com.pravesh.exception.ResourceNotFoundException;
import com.pravesh.repository.FlatRepository;
import com.pravesh.repository.GateRepository;
import com.pravesh.repository.GuardRepository;
import com.pravesh.repository.ResidentRepository;
import com.pravesh.repository.SocietyAdminRepository;
import com.pravesh.repository.UserRepository;
import com.pravesh.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.pravesh.entity.Society;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SOCIETY_ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final GuardRepository guardRepository;
    private final GateRepository gateRepository;
    private final FlatRepository flatRepository;
    private final SocietyAdminRepository societyAdminRepository;

    @GetMapping
    public ApiResponse<List<UserProfileResponse>> listUsers(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestParam(required = false) String role) {

        Long adminSocietyId = caller.societyId();
        List<User> allUsers = (role != null && !role.isBlank())
                ? userRepository.findByRole(Role.valueOf(role.toUpperCase()))
                : userRepository.findAll();

        var result = allUsers.stream()
                .filter(u -> belongsToSociety(u, adminSocietyId))
                .map(u -> new UserProfileResponse(
                        u.getId(), u.getName(), u.getEmail(), u.getPhone(),
                        u.getRole().name(), u.getState(), u.isActive(),
                        null, null, null, null, null, null, null))
                .toList();

        return ApiResponse.ok("Users", result);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> toggleStatus(@PathVariable Long id, @RequestBody StatusRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(req.isActive());
        userRepository.save(user);
        return ApiResponse.ok(req.isActive() ? "User activated" : "User deactivated");
    }

    private boolean belongsToSociety(User u, Long societyId) {
        if (societyId == null) return false;
        switch (u.getRole()) {
            case RESIDENT:
                return residentRepository.findById(u.getId())
                        .map(r -> r.getFlatId() != null &&
                                flatRepository.findById(r.getFlatId())
                                        .map(f -> societyId.equals(f.getSocietyId()))
                                        .orElse(false))
                        .orElse(false);
            case GUARD:
                return guardRepository.findById(u.getId())
                        .map(g -> gateRepository.findById(g.getGateId())
                                .map(gate -> societyId.equals(gate.getSocietyId()))
                                .orElse(false))
                        .orElse(false);
            case SOCIETY_ADMIN:
                return societyAdminRepository.findById(u.getId())
                        .map(a -> societyId.equals(a.getSocietyId()))
                        .orElse(false);
            default:
                return false;
        }
    }

    public record StatusRequest(boolean isActive) {}
}