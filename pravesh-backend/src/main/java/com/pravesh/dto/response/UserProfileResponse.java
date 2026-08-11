package com.pravesh.dto.response;

public record UserProfileResponse(
        Long id, String name, String email, String phone, String role, String state,
        boolean active, String verificationStatus,
        Long flatId, Long gateId, Long societyId,
        String flatNumber, String tower, String societyName   // NEW
) {}