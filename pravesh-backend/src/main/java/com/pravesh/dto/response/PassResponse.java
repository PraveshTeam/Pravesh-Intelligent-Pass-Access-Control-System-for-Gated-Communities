package com.pravesh.dto.response;

import java.time.LocalDateTime;

public record PassResponse(
        Long id,
        String uuid,
        String visitorName,
        String visitorPhone,
        String passType,
        Integer usesAllowed,
        Integer usesRemaining,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        String status,
        String qrBase64
) {}