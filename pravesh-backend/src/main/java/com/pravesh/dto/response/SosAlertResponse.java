package com.pravesh.dto.response;

import com.pravesh.entity.SosCategory;
import com.pravesh.entity.SosStatus;
import java.time.LocalDateTime;

public record SosAlertResponse(
        Long id,
        String residentName,
        String flatNumber,
        String phone,
        SosCategory category,
        String description,
        SosStatus status,
        LocalDateTime createdAt,
        LocalDateTime acknowledgedAt,
        LocalDateTime resolvedAt
) {}