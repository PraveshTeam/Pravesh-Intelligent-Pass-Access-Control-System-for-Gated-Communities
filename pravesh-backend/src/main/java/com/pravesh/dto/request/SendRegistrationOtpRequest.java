package com.pravesh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendRegistrationOtpRequest(

        @NotBlank(message = "contactType is required")
        @Pattern(regexp = "^(EMAIL|PHONE)$", message = "contactType must be EMAIL or PHONE")
        String contactType,

        @NotBlank(message = "value is required")
        String value
) {}
