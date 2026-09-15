package com.pravesh.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ForgotPasswordRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "A valid email is required")
        String email,

        @Pattern(regexp = "^(EMAIL|SMS|BOTH)$", message = "Channel must be EMAIL, SMS, or BOTH")
        String channel // optional, defaults to BOTH if not provided
) {}