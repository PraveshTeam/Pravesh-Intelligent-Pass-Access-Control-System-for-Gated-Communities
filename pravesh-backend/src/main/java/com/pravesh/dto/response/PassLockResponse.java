package com.pravesh.dto.response;

import java.time.LocalDateTime;

public record PassLockResponse(
        Long id,
        String uuid,
        Long residentId,
        String visitorName,
        String passType,
        Integer usesRemaining,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        String status
) {}