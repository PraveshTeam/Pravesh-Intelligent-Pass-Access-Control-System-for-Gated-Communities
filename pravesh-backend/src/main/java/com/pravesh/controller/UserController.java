package com.pravesh.controller;

import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.UserProfileResponse;
import com.pravesh.entity.Flat;
import com.pravesh.entity.Gate;
import com.pravesh.entity.Society;
import com.pravesh.entity.User;
import com.pravesh.entity.enums.Role;
import com.pravesh.exception.ResourceNotFoundException;
import com.pravesh.repository.GuardRepository;
import com.pravesh.repository.ResidentRepository;
import com.pravesh.repository.SocietyAdminRepository;
import com.pravesh.repository.UserRepository;
import com.pravesh.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final GuardRepository guardRepository;
    private final SocietyAdminRepository societyAdminRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMe(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Profile", buildProfile(caller.userId())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMe(
            @AuthenticationPrincipal AuthenticatedUser caller,
            @RequestBody UpdateProfileRequest req) {

        User user = userRepository.findById(caller.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (req.name() != null && !req.name().isBlank()) {
            user.setName(req.name());
        }
        if (req.phone() != null && !req.phone().isBlank()) {
            user.setPhone(req.phone());
        }
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.ok("Profile updated", buildProfile(caller.userId())));
    }

    private UserProfileResponse buildProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String verificationStatus = null;
        Long flatId = null, gateId = null, societyId = null;
        String flatNumber = null, tower = null, societyName = null;

        if (user.getRole() == Role.RESIDENT) {
            var r = residentRepository.findById(user.getId()).orElse(null);
            if (r != null) {
                verificationStatus = r.getVerificationStatus().name();
                Flat flat = r.getFlat();
                if (flat != null) {
                    flatId = flat.getId();
                    flatNumber = flat.getFlatNumber();
                    tower = flat.getTower();
                    Society society = flat.getSociety();
                    if (society != null) {
                        societyId = society.getId();
                        societyName = society.getName();
                    }
                }
            }
        } else if (user.getRole() == Role.GUARD) {
            var g = guardRepository.findById(user.getId()).orElse(null);
            if (g != null) {
                Gate gate = g.getGate();
                if (gate != null) {
                    gateId = gate.getId();
                    Society society = gate.getSociety();
                    if (society != null) {
                        societyId = society.getId();
                        societyName = society.getName();
                    }
                }
            }
        } else if (user.getRole() == Role.SOCIETY_ADMIN) {
            var a = societyAdminRepository.findById(user.getId()).orElse(null);
            if (a != null) {
                verificationStatus = a.getVerificationStatus().name();
                Society society = a.getSociety();
                if (society != null) {
                    societyId = society.getId();
                    societyName = society.getName();
                }
            }
        }

        return new UserProfileResponse(
                user.getId(), user.getName(), user.getEmail(), user.getPhone(),
                user.getRole().name(), user.getState(), user.isActive(),
                verificationStatus, flatId, gateId, societyId,
                flatNumber, tower, societyName);
    }

    public record UpdateProfileRequest(String name, String phone) {}
}