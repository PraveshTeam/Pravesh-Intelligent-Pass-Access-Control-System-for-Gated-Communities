package com.pravesh.dto.request;

public record ResidentApprovedRequest(
        Long residentId,
        String flatNumber
) {}