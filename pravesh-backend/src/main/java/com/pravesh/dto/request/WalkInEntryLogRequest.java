package com.pravesh.dto.request;

public record WalkInEntryLogRequest(
        Long residentId,
        String visitorName,
        Long guardId,
        Long gateId,
        Long societyId,
        String outcome,     // "GRANTED", "DENIED", or "NO_RESPONSE"
        String denyReason   // e.g. "RESIDENT_DENIED" or "RESIDENT_NO_RESPONSE" — null when GRANTED
) {}
