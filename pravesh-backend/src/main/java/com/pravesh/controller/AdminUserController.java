package com.pravesh.controller;

import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.UserProfileResponse;
import com.pravesh.entity.User;
import com.pravesh.entity.enums.Role;
import com.pravesh.exception.ResourceNotFoundException;
import com.pravesh.repository.GuardRepository;
import com.pravesh.repository.ResidentRepository;
import com.pravesh.repository.SocietyAdminRepository;
import com.pravesh.repository.UserRepository;
import com.pravesh.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final SocietyAdminRepository societyAdminRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserProfileResponse>>> listUsers(
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

        return ResponseEntity.ok(ApiResponse.ok("Users", result));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> toggleStatus(@PathVariable Long id, @RequestBody StatusRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setActive(req.isActive());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok(req.isActive() ? "User activated" : "User deactivated"));
    }

    private boolean belongsToSociety(User u, Long societyId) {
        if (societyId == null) return false;
        // Was: nested findById chains (Resident->Flat, Guard->Gate) -- now
        // single navigations off the already-loaded related entity via the
        // mapped relationships.
        switch (u.getRole()) {
            case RESIDENT:
                return residentRepository.findById(u.getId())
                        .map(r -> r.getFlat() != null && societyId.equals(r.getFlat().getSocietyId()))
                        .orElse(false);
            case GUARD:
                return guardRepository.findById(u.getId())
                        .map(g -> g.getGate() != null && societyId.equals(g.getGate().getSocietyId()))
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