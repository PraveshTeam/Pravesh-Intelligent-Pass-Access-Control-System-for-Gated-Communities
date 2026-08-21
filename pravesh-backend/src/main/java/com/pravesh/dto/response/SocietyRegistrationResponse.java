package com.pravesh.dto.response;

import java.time.LocalDateTime;

public record SocietyRegistrationResponse(
        Long id,
        Long adminUserId,
        String adminName,
        String societyName,
        String address,
        String city,
        String status,
        String adminNotes,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt
) {}