package com.pravesh.dto.response;

public record FlatConflictErrorResponse(
        boolean success,
        String message,
        boolean conflict,
        Long occupantResidentId,
        String occupantName,
        String flatNumber
) {}