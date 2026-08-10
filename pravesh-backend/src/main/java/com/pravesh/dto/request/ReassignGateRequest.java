package com.pravesh.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReassignGateRequest(
        @NotNull(message = "New gate is required")
        Long newGateId
) {}