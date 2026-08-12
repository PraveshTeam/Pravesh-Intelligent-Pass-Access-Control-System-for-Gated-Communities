package com.pravesh.dto.request;

import jakarta.validation.constraints.NotNull;

public record CreateSosRequest(
        @NotNull(message = "Category is required")
        String category,

        String description
) {}