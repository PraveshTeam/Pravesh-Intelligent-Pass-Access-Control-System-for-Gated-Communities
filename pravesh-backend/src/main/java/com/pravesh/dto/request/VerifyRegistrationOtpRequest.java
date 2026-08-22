package com.pravesh.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyRegistrationOtpRequest(

        @NotBlank(message = "contactType is required")
        @Pattern(regexp = "^(EMAIL|PHONE)$", message = "contactType must be EMAIL or PHONE")
        String contactType,

        @NotBlank(message = "value is required")
        String value,

        @NotBlank(message = "OTP is required")
        @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
        String otp
) {}
