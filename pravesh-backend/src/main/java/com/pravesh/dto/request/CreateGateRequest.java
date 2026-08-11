package com.pravesh.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateGateRequest(
        @NotBlank(message = "Gate name is required")
        String name,
        String location
) {}