package com.pravesh.dto.request;

public record SocietyAdminApprovedRequest(
        Long adminId,
        String societyName
) {}