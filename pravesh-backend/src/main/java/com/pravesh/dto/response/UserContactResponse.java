package com.pravesh.dto.response;

public record UserContactResponse(
        Long id,
        String name,
        String email,
        String phone
) {}