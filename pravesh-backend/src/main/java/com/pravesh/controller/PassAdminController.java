package com.pravesh.controller;

import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.PassResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.PassService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/passes")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SOCIETY_ADMIN')")
public class PassAdminController {

    private final PassService passService;

    @GetMapping
    public ApiResponse<List<PassResponse>> allPassesInSociety(
            @AuthenticationPrincipal AuthenticatedUser caller) {
        return ApiResponse.ok("All passes in society",
                passService.getAllPassesInSociety(caller.societyId()));
    }
}