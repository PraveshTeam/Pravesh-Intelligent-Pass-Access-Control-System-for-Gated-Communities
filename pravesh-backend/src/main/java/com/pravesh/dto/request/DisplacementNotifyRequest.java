package com.pravesh.dto.request;

public record DisplacementNotifyRequest(
        Long residentUserId,
        String residentPhone,
        String oldFlatNumber
) {}