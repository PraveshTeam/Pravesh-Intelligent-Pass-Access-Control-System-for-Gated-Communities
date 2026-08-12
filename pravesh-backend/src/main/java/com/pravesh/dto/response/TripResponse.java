package com.pravesh.dto.response;

import com.pravesh.entity.TripStatus;
import java.time.LocalDateTime;

public record TripResponse(
        Long id,
        Long creatorId,
        String creatorName,
        String title,
        String description,
        int capacity,
        int acceptedCount,
        TripStatus status,
        LocalDateTime createdAt,
        String myRequestStatus 
) {}