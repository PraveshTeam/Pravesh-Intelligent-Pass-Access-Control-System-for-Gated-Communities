package com.pravesh.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ShiftCheckinRequest(

        @NotBlank(message = "On-duty name is required")
        String onDutyName,

        String onDutyEmployeeId
) {}