package com.pravesh.dto.response;

public record GateResponse(
        Long id,
        String name,
        String location,
        boolean hasAssignedGuard
) {}