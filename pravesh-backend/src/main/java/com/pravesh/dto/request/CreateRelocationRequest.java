package com.pravesh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRelocationRequest(
        @NotNull(message = "Target society is required")
        Long targetSocietyId,

        @NotBlank(message = "Claimed flat number is required")
        String claimedFlatNumber,

        String tower,

        @NotBlank(message = "Document type is required")
        String documentType
) {}