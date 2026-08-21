package com.pravesh.dto.request;

import jakarta.validation.constraints.Pattern;

public record RequestDecisionRequest(
        @Pattern(regexp = "ACCEPTED|REJECTED", message = "status must be ACCEPTED or REJECTED")
        String status
) {}
