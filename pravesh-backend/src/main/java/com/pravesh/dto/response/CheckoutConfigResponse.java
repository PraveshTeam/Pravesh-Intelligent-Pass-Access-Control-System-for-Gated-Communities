package com.pravesh.dto.response;

import java.math.BigDecimal;

// Everything the frontend's Razorpay Checkout.js needs to open the payment sheet.
// The frontend NEVER sees the key secret — only the public key id and the order id.
public record CheckoutConfigResponse(
        Long paymentOrderId,
        String razorpayKeyId,
        String razorpayOrderId,
        BigDecimal amount,
        String currency
) {}
