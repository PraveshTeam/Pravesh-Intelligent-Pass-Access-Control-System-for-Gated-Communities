package com.pravesh.dto.request;

import com.pravesh.entity.enums.PassType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record CreatePassRequest(

        @NotBlank(message = "Visitor name is required")
        String visitorName,

        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Enter a valid 10-digit phone number")
        String visitorPhone,

        @NotNull(message = "Pass type is required")
        PassType passType,

        Integer usesAllowed, // required only for MULTI_USE, checked in service

        @NotNull(message = "Valid-from time is required")
        LocalDateTime validFrom,

        @NotNull(message = "Valid-until time is required")
        LocalDateTime validUntil
) {}