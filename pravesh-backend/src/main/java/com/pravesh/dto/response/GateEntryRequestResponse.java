package com.pravesh.dto.response;

import com.pravesh.entity.GateRequestStatus;
import java.time.LocalDateTime;

public record GateEntryRequestResponse(
        Long id,
        String visitorName,
        String visitorPhone,
        String claimedFlatNumber,
        String reason,
        GateRequestStatus status,
        LocalDateTime createdAt,
        LocalDateTime expiresAt
) {}