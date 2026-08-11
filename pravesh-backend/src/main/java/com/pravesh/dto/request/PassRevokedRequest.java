package com.pravesh.dto.request;

public record PassRevokedRequest(
        Long residentId,
        String visitorName,
        String passUuid
) {}