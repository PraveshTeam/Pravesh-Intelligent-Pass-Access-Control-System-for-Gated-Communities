package com.pravesh.dto.request;

public record VisitorEnteredRequest(
        Long residentId,
        String visitorName,
        String gateName,
        String scannedAt
) {}