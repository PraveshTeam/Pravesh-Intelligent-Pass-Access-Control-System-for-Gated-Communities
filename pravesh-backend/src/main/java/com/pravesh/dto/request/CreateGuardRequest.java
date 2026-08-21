package com.pravesh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateGuardRequest(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit phone number")
        String phone,

        Long gateId,           // provide this OR the two fields below — not both required
        String newGateName,
        String newGateLocation
) {}