package com.pravesh.dto.response;

import java.time.LocalDateTime;

public record OnboardingRequestResponse(
        Long id,
        Long userId,
        String userName,
        String claimedFlatNumber,
        String tower,
        String documentType,
        String status,
        String adminNotes,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt
) {}