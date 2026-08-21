package com.pravesh.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateGateEntryRequest(
        @NotBlank(message = "Visitor name is required")
        String visitorName,

        String visitorPhone,

        @NotBlank(message = "Claimed flat number is required")
        String claimedFlatNumber,

        String reason
) {}