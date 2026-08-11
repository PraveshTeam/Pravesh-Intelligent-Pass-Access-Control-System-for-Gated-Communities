package com.pravesh.dto.response;

public record ResidentContextResponse(
        Long userId, String name, String phone,
        Long flatId, String flatNumber, Long societyId) {}
