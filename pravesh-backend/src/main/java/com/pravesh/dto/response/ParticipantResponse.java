package com.pravesh.dto.response;

public record ParticipantResponse(
        Long userId,
        String name,
        String phone,
        String flatNumber,
        boolean isCreator
) {}
