package com.pravesh.dto.response;

import com.pravesh.entity.enums.RequestStatus;
import java.time.LocalDateTime;

public record RelocationRequestResponse(
        Long id,
        String residentName,
        String oldFlatNumber,
        String oldSocietyName,
        String claimedFlatNumber,
        String targetSocietyName,
        String documentType,
        RequestStatus status,
        String adminNotes,
        LocalDateTime createdAt
) {}