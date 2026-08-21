package com.pravesh.dto.response;

import com.pravesh.entity.SosStatus;
import java.time.LocalDateTime;

public record SosStatusHistoryResponse(
        SosStatus status,
        Long changedByUserId,
        String changedByName,  // resolved via feign; falls back to null gracefully
        LocalDateTime changedAt
) {}
