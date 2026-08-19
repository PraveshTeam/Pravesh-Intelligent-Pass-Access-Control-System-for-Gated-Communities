package com.pravesh.dto.response;

public record FlatResponse(
        Long id,
        String flatNumber,
        String tower,
        Long residentId
) {}