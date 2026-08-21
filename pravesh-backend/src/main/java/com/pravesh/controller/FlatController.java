package com.pravesh.controller;

import com.pravesh.dto.response.ApiResponse;
import com.pravesh.dto.response.FlatResponse;
import com.pravesh.security.AuthenticatedUser;
import com.pravesh.service.FlatService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/flats")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SOCIETY_ADMIN')")
public class FlatController {

    private final FlatService flatService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FlatResponse>>> listFlats(@AuthenticationPrincipal AuthenticatedUser caller) {
        return ResponseEntity.ok(ApiResponse.ok("Flats in your society", flatService.listFlats(caller.societyId())));
    }
}