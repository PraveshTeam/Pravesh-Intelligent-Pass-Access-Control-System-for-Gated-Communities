package com.pravesh.dto.request;

public record GuardCredentialsRequest(
        String phone,
        String tempPassword,
        String gateName
) {}