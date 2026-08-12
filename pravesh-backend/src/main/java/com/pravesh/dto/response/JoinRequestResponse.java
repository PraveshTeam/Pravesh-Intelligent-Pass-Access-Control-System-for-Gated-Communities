package com.pravesh.dto.response;

import com.pravesh.entity.JoinRequestStatus;
import java.time.LocalDateTime;

public record JoinRequestResponse(
        Long id,
        Long tripId,
        Long requesterId,
        String requesterName,
        JoinRequestStatus status,
        LocalDateTime createdAt
) {}
