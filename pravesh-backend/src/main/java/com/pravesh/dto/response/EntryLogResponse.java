package com.pravesh.dto.response;

import java.time.LocalDateTime;

public record EntryLogResponse(
        Long id,
        String visitorName,
        Long residentId,
        String entryType,
        String scanResult,
        String denyReason,
        LocalDateTime scannedAt
) {}