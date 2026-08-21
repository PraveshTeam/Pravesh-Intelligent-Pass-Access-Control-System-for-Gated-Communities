package com.pravesh.dto.response;

public record FlatConflictResponse(
        Long occupantResidentId,
        String occupantName,
        String flatNumber
) {}