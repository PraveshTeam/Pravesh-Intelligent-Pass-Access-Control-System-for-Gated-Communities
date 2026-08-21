package com.pravesh.controller;

import com.pravesh.dto.response.ApiResponse;
import com.pravesh.entity.Society;
import com.pravesh.repository.SocietyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/societies")
@RequiredArgsConstructor
public class SocietyController {

    private final SocietyRepository societyRepository;

    // Any authenticated user can list societies to find their own during onboarding.
    @GetMapping
    public ResponseEntity<ApiResponse<List<SocietyListItem>>> listSocieties(
            @RequestParam(required = false) String search) {

        List<Society> societies = (search != null && !search.isBlank())
                ? societyRepository.findByNameContainingIgnoreCase(search)
                : societyRepository.findAll();

        var result = societies.stream()
                .map(s -> new SocietyListItem(s.getId(), s.getName(), s.getAddress(), s.getCity()))
                .toList();

        return ResponseEntity.ok(ApiResponse.ok("Societies", result));
    }

    public record SocietyListItem(Long id, String name, String address, String city) {}
}