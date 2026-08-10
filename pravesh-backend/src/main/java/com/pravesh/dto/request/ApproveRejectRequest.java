package com.pravesh.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ApproveRejectRequest(
        String reason // required only for reject; validated in the service
) {}