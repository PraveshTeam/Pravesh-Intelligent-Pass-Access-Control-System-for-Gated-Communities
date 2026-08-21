package com.pravesh.dto.response;

import com.pravesh.entity.PaymentPurpose;
import com.pravesh.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// residentName/flatNumber are only populated for the admin listing (enriched
// via a Feign call to user-service); a resident's own history endpoint doesn't
// need them -- they already know it's their own payment -- so they'll be null there.
public record PaymentOrderResponse(
        Long id,
        Long residentId,
        String residentName,
        String flatNumber,
        PaymentPurpose purpose,
        Long referenceId,
        BigDecimal amount,
        String razorpayOrderId,
        PaymentStatus status,
        LocalDateTime createdAt,
        LocalDateTime paidAt
) {}
