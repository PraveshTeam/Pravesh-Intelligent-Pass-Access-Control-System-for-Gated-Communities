package com.pravesh.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CreatePaymentOrderRequest(

        @NotNull(message = "Purpose is required")
        @Pattern(regexp = "MAINTENANCE|EVENT|ACTIVITY|TRIP", message = "Purpose must be MAINTENANCE, EVENT, ACTIVITY, or TRIP")
        String purpose,

        Long referenceId, // required only for EVENT/ACTIVITY/TRIP, validated in service layer

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "1.00", message = "Amount must be at least ₹1")
        BigDecimal amount

) {}
