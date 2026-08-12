package com.pravesh.dto.response;

public record SummaryResponse(
        long totalEntries,
        long totalGranted,
        long totalDenied,
        long uniqueVisitors
) {}