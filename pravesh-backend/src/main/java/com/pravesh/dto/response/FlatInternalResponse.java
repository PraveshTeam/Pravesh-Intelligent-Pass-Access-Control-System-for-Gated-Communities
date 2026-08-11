package com.pravesh.dto.response;

public record FlatInternalResponse(
        Long id,
        Long societyId,
        String flatNumber,
        String tower,
        Long residentId
) {}