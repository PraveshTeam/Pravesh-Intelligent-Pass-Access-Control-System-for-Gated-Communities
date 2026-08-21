package com.pravesh.dto.request;

public record GateEntryNotifyRequest(
        Long residentUserId,
        String residentPhone,
        String visitorName,
        String flatNumber,
        Long requestId
) {}