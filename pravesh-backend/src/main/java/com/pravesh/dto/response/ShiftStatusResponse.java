package com.pravesh.dto.response;

public record ShiftStatusResponse(
        boolean hasActiveShift,
        Long shiftId
) {}